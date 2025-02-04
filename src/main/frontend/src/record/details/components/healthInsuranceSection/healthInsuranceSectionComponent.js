/*
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

import {JUNO_STYLE} from "../../../../common/components/junoComponentConstants";
import HinValidator from "../../../../common/util/hinValidator";

angular.module('Record.Details').component('healthInsuranceSection', {
	templateUrl: 'src/record/details/components/healthInsuranceSection/healthInsuranceSection.jsp',
	bindings: {
		ngModel: "=",
		validations: "=",
		componentStyle: "<?"
	},
	controller: [ "staticDataService", "$scope", function (staticDataService, $scope)
	{
		let ctrl = this;

		ctrl.provinces = staticDataService.getProvinces();

		ctrl.effectiveDateValid = true;

		ctrl.$onInit = () =>
		{
			ctrl.componentStyle = ctrl.componentStyle || JUNO_STYLE.DEFAULT

			ctrl.validations = Object.assign(ctrl.validations, {
				hin: Juno.Validations.validationCustom(() =>
				{
					if (ctrl.ngModel)
					{
						if (ctrl.ngModel.hin)
						{
							return HinValidator.healthCardNumber(ctrl.ngModel.hcType, ctrl.ngModel.hin);
						}
						else
						{
							return true;
						}
					}
					return false;
				}),
				hinEffectiveDate: Juno.Validations.validationCustom(() => ctrl.effectiveDateValid),
			});
		}

	}]
});