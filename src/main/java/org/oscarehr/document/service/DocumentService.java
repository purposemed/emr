/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

package org.oscarehr.document.service;

import org.apache.log4j.Logger;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.common.dao.PatientLabRoutingDao;
import org.oscarehr.common.dao.ProviderInboxRoutingDao;
import org.oscarehr.common.io.FileFactory;
import org.oscarehr.common.io.GenericFile;
import org.oscarehr.common.model.CtlDocumentPK;
import org.oscarehr.common.model.PatientLabRouting;
import org.oscarehr.document.dao.CtlDocumentDao;
import org.oscarehr.document.dao.DocumentDao;
import org.oscarehr.document.model.CtlDocument;
import org.oscarehr.document.model.Document;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import oscar.MyDateFormat;
import oscar.OscarProperties;
import oscar.dms.EDocUtil;
import oscar.dms.data.AddEditDocumentForm;
import oscar.log.LogAction;
import oscar.log.LogConst;
import oscar.oscarLab.ca.on.LabResultData;
import oscar.util.ConversionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static oscar.util.StringUtils.filled;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DocumentService
{
	private static final OscarProperties props = OscarProperties.getInstance();
	private static Logger logger = MiscUtils.getLogger();

	@Autowired
	DocumentDao documentDao;

	@Autowired
	CtlDocumentDao ctlDocumentDao;

	@Autowired
	ProviderInboxRoutingDao providerInboxRoutingDao;

	@Autowired
	PatientLabRoutingDao patientLabRoutingDao;

	@Autowired
	ProgramManager programManager;

	/**
	 * Create a new document from the given document model and a file input stream.
	 * This method will write the file from the stream and persist the document record.
	 * Document metadata fields that can be read from the file directly will replace the values in the model.
	 * @param document - the un-persisted document model
	 * @param fileInputStream - input stream
	 * @param demographicNo - demographic id of the attached demographic
	 * @return - the persisted document model
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Document uploadNewDocument(Document document, InputStream fileInputStream, Integer demographicNo) throws IOException, InterruptedException
	{
		GenericFile file = FileFactory.createDocumentFile(fileInputStream, document.getDocfilename());

		if(!file.validate())
		{
			file.reEncode();
		}
		file.moveToDocuments();

		document.setDocfilename(file.getName());
		document.setContenttype(file.getContentType());
		document.setNumberofpages(file.getPageCount());

		// default some values if they are missing
		if(document.getObservationdate() == null)
		{
			document.setObservationdate(new Date());
		}
		if(document.getContentdatetime() == null)
		{
			document.setContentdatetime(new Date());
		}
		if(document.getProgramId() == null)
		{
			document.setProgramId(programManager.getDefaultProgramId());
		}
		documentDao.persist(document);

		if(demographicNo == null || demographicNo < 1)
		{
			// unassigned documents still get a link with id -1
			createDemographicCtlLink(document, -1);
		}
		else
		{
			assignDocumentToDemographic(document, demographicNo);
		}
		logger.info("Uploaded Document " + document.getDocumentNo());
		return document;
	}
	public Document uploadNewDocument(Document document, InputStream fileInputStream) throws IOException, InterruptedException
	{
		return uploadNewDocument(document, fileInputStream, null);
	}

	public void updateDocument(AddEditDocumentForm fm,
	                           InputStream documentInputStream,
	                           String documentFileName,
	                           Integer documentNo,
	                           Integer demographicNo,
	                           String providerNo,
	                           String userIP,
	                           Integer programId
	) throws IOException, InterruptedException
	{
		//TODO - replace AddEditDocumentForm with transfer object

		// load existing models & retrieve file object
		Document documentModel = documentDao.find(documentNo);
		if(documentModel == null)
		{
			throw new IllegalArgumentException("No Document exists with documentId " + documentNo);
		}
		CtlDocument ctlDocumentModel = ctlDocumentDao.getCtrlDocument(documentNo);
		if(ctlDocumentModel == null)
		{
			throw new IllegalStateException("No CtlDocument exists for documentId " + documentNo);
		}
		GenericFile file = FileFactory.getDocumentFile(documentModel.getDocfilename());

		boolean hasFileChanges = (documentFileName != null && !documentFileName.trim().isEmpty() && documentInputStream != null);
		boolean allowContentOverwrite = props.isPropertyActive("ALLOW_UPDATE_DOCUMENT_CONTENT");
		String formattedFileName = null;
		GenericFile tempFile = null;

		boolean isReview = fm.getReviewDoc();
		Date timestamp = new Date(); // still a legacy date
		boolean isPublicDoc = ("1".equals(fm.getDocPublic()) || "checked".equalsIgnoreCase(fm.getDocPublic()));

		// update the model info
		documentModel.setDocdesc(fm.getDocDesc());
		documentModel.setDoctype(fm.getDocType());
		documentModel.setDoccreator(fm.getDocCreator());
		documentModel.setResponsible(fm.getResponsibleId());
		documentModel.setObservationdate(MyDateFormat.getSysDate(fm.getObservationDate()));
		documentModel.setSource(fm.getSource());
		documentModel.setSourceFacility(fm.getSourceFacility());
		documentModel.setPublic1(isPublicDoc);
		documentModel.setAppointmentNo(Integer.parseInt(fm.getAppointmentNo()));
		documentModel.setDocClass(fm.getDocClass());
		documentModel.setDocSubClass(fm.getDocSubClass());
		if (programId != null)
		{
			documentModel.setProgramId(programId);
		}

		if(hasFileChanges)
		{
			// replace an existing file with the new content
			if(allowContentOverwrite)
			{
				formattedFileName = GenericFile.getFormattedFileName(documentFileName);
				// get a tempfile. it will replace the existing doc at last step of the transaction
				tempFile = FileFactory.createTempFile(documentInputStream);
				// validate the file & content
				if(!tempFile.validate())
				{
					tempFile.reEncode();
				}
			}
			// save the content as new and update the references, but keep the previous document in the folder.
			else
			{
				tempFile = FileFactory.createDocumentFile(documentInputStream, documentFileName);
				formattedFileName = tempFile.getName();
				// validate the file & content
				if(!tempFile.validate())
				{
					tempFile.reEncode();
				}
				tempFile.moveToDocuments();
			}
			documentModel.setDocfilename(formattedFileName);
			documentModel.setContenttype(tempFile.getContentType());
			documentModel.setNumberofpages(tempFile.getPageCount());
			documentModel.setContentdatetime(timestamp);
		}

		if(isReview)
		{
			String reviewerId = filled(fm.getReviewerId()) ? fm.getReviewerId() : providerNo;
			String reviewDateTimeStr = filled(fm.getReviewDateTime()) ? fm.getReviewDateTime() : null;
			Date reviewDateTime = ConversionUtils.fromDateString(reviewDateTimeStr, EDocUtil.REVIEW_DATETIME_FORMAT);
			reviewDateTime = (reviewDateTime == null)? timestamp : reviewDateTime;

			documentModel.setReviewer(reviewerId);
			documentModel.setReviewdatetime(reviewDateTime);
			LogAction.addLogEntry(reviewerId, demographicNo, LogConst.ACTION_REVIEWED, LogConst.CON_DOCUMENT, LogConst.STATUS_SUCCESS,
					String.valueOf(documentNo), userIP, documentModel.getDocfilename());
		}
		else
		{
			// review info is removed if the document is modified.
			documentModel.setReviewer(null);
			documentModel.setReviewdatetime(null);
		}

		documentDao.merge(documentModel);

		LogAction.addLogEntry(providerNo, demographicNo, LogConst.ACTION_UPDATE, LogConst.CON_DOCUMENT, LogConst.STATUS_SUCCESS,
				String.valueOf(documentNo), userIP, documentModel.getDocfilename());

		// Important to do the file overwrite/rename after the database operations.
		// Transaction can roll back if file move fails, but file move can't roll back if db operation fails.
		if(hasFileChanges && allowContentOverwrite)
		{
			file.replaceWith(tempFile);
			file.rename(formattedFileName);
		}
	}


	/**
	 * Add the document to the given provider inbox
	 * @param documentNo - document id to route
	 * @param providerNo - provider id to route to
	 */
	public void routeToProviderInbox(Integer documentNo, Integer providerNo)
	{
		//TODO handle the routing weirdness
		providerInboxRoutingDao.addToProviderInbox(String.valueOf(providerNo), documentNo, LabResultData.DOCUMENT);
		logger.info("Added route to provider " + providerNo + " for document " + documentNo);
	}
	/**
	 * Add the document to the unclaimed/general inbox
	 * @param documentNo - document id to route
	 */
	public void routeToGeneralInbox(Integer documentNo)
	{
		routeToProviderInbox(documentNo, 0);
	}

	/**
	 * Assign the given document to a demographic record
	 * @param document - the document to assign
	 * @param demographicNo - the demographic id to assign to
	 */
	public void assignDocumentToDemographic(Document document, Integer demographicNo)
	{
		CtlDocument ctlDocument = ctlDocumentDao.getCtrlDocument(document.getDocumentNo());
		if(ctlDocument != null)
		{
			// since the demographic module id is a primary key, the old entry can't be updated by jpa. so we replace it instead.
			ctlDocumentDao.remove(ctlDocument);
		}
		createDemographicCtlLink(document, demographicNo);
		createDemographicRouteLink(document, demographicNo);
	}
	/**
	 * Assign the given document to a demographic record
	 * @param documentNo - the document id to assign
	 * @param demographicNo - the demographic id to assign to
	 */
	public void assignDocumentToDemographic(Integer documentNo, Integer demographicNo)
	{
		Document document = documentDao.find(documentNo);
		assignDocumentToDemographic(document, demographicNo);
	}

	private void createDemographicRouteLink(Document document, Integer demographicNo)
	{
		// Check to see if we have to route document to patient
		List<PatientLabRouting> patientLabRoutingList = patientLabRoutingDao.findByLabNoAndLabType(document.getDocumentNo(), PatientLabRoutingDao.DOC);
		if(patientLabRoutingList.isEmpty())
		{
			PatientLabRouting patientLabRouting = new PatientLabRouting();
			patientLabRouting.setDemographicNo(demographicNo);
			patientLabRouting.setLabNo(document.getDocumentNo());
			patientLabRouting.setLabType(PatientLabRoutingDao.DOC);
			patientLabRoutingDao.persist(patientLabRouting);
		}
		//TODO handle re-assigning a linked document?
	}

	private void createDemographicCtlLink(Document document, Integer demographicNo)
	{
		CtlDocumentPK cdpk = new CtlDocumentPK();
		CtlDocument cd = new CtlDocument();
		cd.setId(cdpk);
		cdpk.setModule(CtlDocument.MODULE_DEMOGRAPHIC);
		cdpk.setDocumentNo(document.getDocumentNo());
		cd.getId().setModuleId(demographicNo);
		cd.setStatus(String.valueOf(document.getStatus()));
		ctlDocumentDao.persist(cd);
	}
}