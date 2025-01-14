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

import {JUNO_BUTTON_COLOR, JUNO_STYLE, JUNO_TAB_TYPE} from "../junoComponentConstants";

angular.module('Common.Components').component('junoTab', {
	templateUrl: 'src/common/components/junoTab/junoTab.jsp',
	bindings: {
		ngModel: "=",
		tabs: "<",
		componentStyle: "<?",
		type: "<?",
		change: "&?"
	},
	controller: ['$scope', function ($scope) {
		let ctrl = this;

		$scope.JUNO_TAB_TYPE = JUNO_TAB_TYPE;
		$scope.JUNO_BUTTON_COLOR = JUNO_BUTTON_COLOR;

		ctrl.$onInit = () =>
		{
			ctrl.componentStyle = ctrl.componentStyle || JUNO_STYLE.DEFAULT;
			ctrl.type = ctrl.type || JUNO_TAB_TYPE.NORMAL;
		};

		ctrl.onTabSelect = (tab) =>
		{
			ctrl.ngModel = tab.value;

			if (ctrl.change)
            {
                ctrl.change({
                    activeTab: tab.value
                });
            }
		}

		ctrl.tabClasses = (tab) =>
		{
			if (ctrl.ngModel)
			{
				if (ctrl.ngModel === tab.value)
				{
					return ["active"];
				}
			}
			return [];
		}

		ctrl.componentClasses = () =>
		{
			return [ctrl.componentStyle, ctrl.type];
		}
	}]
});
