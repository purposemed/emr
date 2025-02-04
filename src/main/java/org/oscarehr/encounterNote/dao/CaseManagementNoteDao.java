/**
 * Copyright (c) 2012-2018. CloudPractice Inc. All Rights Reserved.
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
 * This software was written for
 * CloudPractice Inc.
 * Victoria, British Columbia
 * Canada
 */
package org.oscarehr.encounterNote.dao;

import org.oscarehr.common.dao.AbstractDao;
import org.oscarehr.encounterNote.model.CaseManagementNote;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("unchecked")
@Transactional
@Repository("encounterNote.dao.CaseManagementNoteDao")
public class CaseManagementNoteDao extends AbstractDao<CaseManagementNote>
{
	public CaseManagementNoteDao()
	{
		super(CaseManagementNote.class);
	}

	public CaseManagementNote findLatestByUUID(String uuid)
	{
		// select model name must match specified @Entity name in model object
		String queryString =
				"SELECT x FROM model.CaseManagementNote x " +
				"WHERE x.uuid = :uuid " +
				"ORDER BY x.noteId DESC";
		Query query = entityManager.createQuery(queryString);
		query.setParameter("uuid", uuid);
		query.setMaxResults(1);

		return this.getSingleResultOrNull(query);
	}

	public List<CaseManagementNote> findByDemographicAndIssue(Integer demographicNo, Long issueId)
	{
		String queryString =
				"SELECT cm FROM model.CaseManagementNote cm " +
				"WHERE cm.demographic.demographicId = :demographicNo " +
				"AND :issueId = ANY (" +
					"SELECT cin.id.caseManagementIssue.issue.issueId " +
					"FROM cm.issueNoteList cin" +
				")";

		Query query = entityManager.createQuery(queryString);
		query.setParameter("demographicNo", demographicNo);
		query.setParameter("issueId", issueId);
		return query.getResultList();
	}

	/**
	* Return the demographic's notes. For each note, return only the latest, non-archived revision.
	 * @param demographicNo the demographic number of the patient in question
	 * @param isCPPNote <code>true</code>if you are trying to get cpp notes,
	 *                  <code>false</code> otherwise.
	 * @return <code>List<CaseManagementNote></code> if there are 1 or more existing notes;
	 * 		   <code>null</code> otherwise.
	 */
	public List<CaseManagementNote> findLatestRevisionOfAllNotes(Integer demographicNo, boolean isCPPNote)
	{
		/* Grabs every column from the casemgmt_note table by joining the following:
		  --casemgmt_note_filter: grabs the most-recently updated version of the note
		  --cpp_note: determines if the note is a cpp note */
		String queryString =
			"SELECT cm.note_id, cm.update_date, cm.observation_date, cm.demographic_no, cm.provider_no, cm.note,\n" +
					"cm.signed, cm.include_issue_innote, cm.signing_provider_no, cm.encounter_type, cm.billing_code, cm.program_no, cm.reporter_caisi_role,\n" +
					"cm.reporter_program_team, cm.history, cm.password, cm.locked, cm.archived, cm.position, cm.uuid, cm.appointmentNo,\n" +
					"cm.hourOfEncounterTime, cm.minuteOfEncounterTime, cm.hourOfEncTransportationTime, cm.minuteOfEncTransportationTime\n" +
			"FROM casemgmt_note cm\n" +
			"LEFT JOIN casemgmt_note cm_filter\n" +
			"ON cm.uuid = cm_filter.uuid\n" +
				"AND (cm_filter.update_date > cm.update_date\n" +
					"OR (cm_filter.update_date = cm.update_date AND cm_filter.note_id > cm.note_id)\n" +
				")\n" +
			"LEFT JOIN (\n" +
				"SELECT note.note_id,\n" +
					"SUM(i.code IN\n" +
					"('OMeds', 'SocHistory', 'MedHistory', 'Concerns', 'FamHistory', 'Reminders', 'RiskFactors', 'OcularMedication', 'TicklerNote')\n" +
				") > 0 AS is_cpp_note\n" +
				"FROM casemgmt_note note\n" +
				"JOIN casemgmt_issue_notes cinotes on note.note_id = cinotes.note_id\n" +
				"JOIN casemgmt_issue ci on cinotes.id = ci.id\n" +
				"JOIN issue i ON ci.issue_id = i.issue_id\n" +
				"WHERE note.demographic_no = :demographicNo\n" +
				"GROUP BY note.note_id\n" +
			")\n" +
			"AS cpp_note ON cpp_note.note_id = cm.note_id\n" +
			"LEFT JOIN casemgmt_note_ext cme " +
				"ON cm.note_id = cme.note_id " +
				"AND cme.key_val = 'Hide Cpp' " +
				"AND cme.value = 1 " +
			"WHERE cm.demographic_no = :demographicNo\n" +
			"AND cme.value IS NULL\n";

		if (isCPPNote)
		{
			queryString +=
				"AND cpp_note.is_cpp_note IS NOT NULL\n";
		}
		else
		{
			queryString +=
				"AND cpp_note.is_cpp_note IS NULL\n";
		}

		queryString +=
			"AND cm.archived = 0\n" +
			"AND cm_filter.note_id IS NULL\n" +
			"ORDER BY cm.observation_date ASC";

		Query query = entityManager.createNativeQuery(queryString, CaseManagementNote.class);
		query.setParameter("demographicNo", demographicNo);

		List<CaseManagementNote> results = query.getResultList();

		return results;
	}
}
