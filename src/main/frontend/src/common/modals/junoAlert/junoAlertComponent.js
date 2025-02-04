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
import {JUNO_ALERT_MODES} from "./junoAlertConstants";
import {JUNO_STYLE} from "../../components/junoComponentConstants";

angular.module('Common.Components').component('junoAlertComponent',
{
	templateUrl: 'src/common/modals/junoAlert/junoAlert.jsp',
	bindings: {
		modalInstance: "<",
		resolve: "<",
	},
	controller: ['$scope', function ($scope)
	{
		let ctrl = this;
		ctrl.alertModes = JUNO_ALERT_MODES;

		ctrl.$onInit = () =>
		{
			ctrl.resolve.style = ctrl.resolve.style || JUNO_STYLE.DEFAULT;
			angular.element(document.querySelector(".modal-content")).addClass(ctrl.resolve.style + "-background-no-accent");
		}

		ctrl.close = function()
		{
			ctrl.modalInstance.close();
		};

		ctrl.onSelection = function(userOk)
		{
			ctrl.modalInstance.close(userOk);
		}

		ctrl.getComponentClasses = () =>
		{
			return [ctrl.resolve.style, ctrl.resolve.style + "-background"]
		}
	}]
});