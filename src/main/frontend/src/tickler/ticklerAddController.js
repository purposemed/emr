import {SystemPreferenceApi} from "../../generated";

angular.module('Tickler').controller('Tickler.TicklerAddController', [

	'$scope',
	'$uibModalInstance',
	'$filter',
	'$stateParams',
	'$http',
	'$httpParamSerializer',
	'demographicService',
	'demographicsService',
	'providerService',
	'ticklerService',

	function(
		$scope,
		$uibModalInstance,
		$filter,
		$stateParams,
		$http,
		$httpParamSerializer,
		demographicService,
		demographicsService,
		providerService,
		ticklerService)
	{
		var controller = this;
		let systemPreferenceApi = new SystemPreferenceApi($http, $httpParamSerializer, '../ws/rs');

		// holds the patient typeahead selection
		controller.demographicSearch = null;
		controller.isDisabled = false; // Save button enabled by default

		controller.defaultTicklerProviderNo = null;
		controller.defaultTicklerProviderName = null;
		//=========================================================================
		// Watches
		//=========================================================================

		// $scope.$watch('controller.demographicSearch',

		// 	function(new_value)
		// 	{
		// 		console.log('watching demographicSearch: ', new_value);

		// 		if (Juno.Common.Util.exists(new_value))
		// 		{
		// 			controller.updateDemographicNo(new_value);
		// 		}
		// 		else
		// 		{
		// 			// no selection
		// 			controller.updateDemographicNo(null);
		// 		}
		// 	}, true);

		controller.tickler = {
			template:
			{
				id: 1,
				name: ''
			},
			serviceDateDate: new Date(),
			serviceDateTime: "12:00 AM",
			suggestedTextId: 0,
			taskAssignedTo: null,
			taskAssignedToName: null,
		};

		controller.priorities = ['Low', 'Normal', 'High'];

		// initialization
		controller.init = function init()
		{
			controller.setTicklerProvider();

			if (Juno.Common.Util.exists($stateParams.demographicNo))
			{
				console.log('initializing demographicSearch pre-selected', $stateParams.demographicNo);
				demographicService.getDemographic($stateParams.demographicNo).then(function(data)
				{
					controller.demographicSearch = {
						demographicNo: data.demographicNo,
						firstName: data.firstName,
						lastName: data.lastName,
						name: data.lastName + "," + data.firstName // For display purposes
					};
					controller.updateDemographicNo(data);
				});
			}

			$('#timepicker').timepicker({defaultTime: controller.tickler.serviceDateTime});
		};

		ticklerService.getTextSuggestions().then(function(data)
		{
			controller.textSuggestions = data.content;
			controller.textSuggestions.unshift(
			{
				id: 0,
				suggestedText: ''
			});
		}, function(reason)
		{
			alert(reason);
		});

		controller.close = function close()
		{
			$uibModalInstance.close(false);
		};

		controller.validate = function validate()
		{
			var t = controller.tickler;
			controller.errors = [];

			if (t.demographic == null)
			{
				controller.errors.push('You must select a patient');
			}
			if (t.taskAssignedTo == null || t.taskAssignedTo.length == 0)
			{
				controller.errors.push('You must assign a provider');
			}
			if (t.message == null || t.message.length == 0)
			{
				controller.errors.push('Message is required');
			}
			if (controller.errors.length > 0)
			{
				return false;
			}
			return true;
		};

		controller.saveWithEncounter = function saveWithEncounter()
		{
			return controller.save(true);
		}

		controller.save = function save(writeEncounter = false)
		{
			controller.isDisabled = true; // Disable save button
			controller.showErrors = true;
			if (!controller.validate())
			{
				controller.isDisabled = false; // Enable save button if validation failed
				return;
			}

			var tickler = {};
			tickler.demographicNo = controller.tickler.demographic.demographicNo;
			tickler.taskAssignedTo = controller.tickler.taskAssignedTo;
			tickler.priority = controller.tickler.priority;
			tickler.status = 'A';
			tickler.message = controller.tickler.message;

			var givenDate = controller.tickler.serviceDateDate;
			var givenTime;
			var midnight = "12:00 AM";
			if (controller.tickler.serviceDateTime === midnight)
			{
				givenTime = moment(controller.tickler.serviceDateTime, 'hh:mm A').add(1, 'minutes');
			}else
			{
				givenTime = moment(controller.tickler.serviceDateTime, 'hh:mm A');
			}
			givenDate.setHours(givenTime.get('hour'));
			givenDate.setMinutes(givenTime.get('minute'));
			givenDate.setSeconds(givenTime.get('second'));

			tickler.serviceDate = givenDate;
            ticklerService.add(tickler, writeEncounter).then(
                (response) =>
                {
                    $uibModalInstance.close(true);
                }).catch((error) =>
                {
                    alert(error);
                }).finally(() =>
                {
                    controller.isDisabled = false;
                });


		};

		controller.updateDemographicNo = function updateDemographicNo(demo)
		{
			if (Juno.Common.Util.exists(demo))
			{
				demographicService.getDemographic(demo.demographicNo).then(function(data)
				{
					// update the selected value on the tickler object
					controller.tickler.demographic = data;
					console.log('set controller.tickler.demographic: ', controller.tickler.demographic);
				});
			}
			else
			{
				controller.tickler.demographic = null;
			}
		};

		controller.searchPatients = function searchPatients(term)
		{
			var search = {
				type: demographicsService.SEARCH_MODE.Name,
				term: term,
				status: demographicsService.STATUS_MODE.ACTIVE,
				integrator: false,
				outofdomain: true
			};
			return demographicsService.search(search, 0, 25).then(
				function(results)
				{
					var resp = [];
					for (var x = 0; x < results.data.length; x++)
					{
						resp.push(
						{
							demographicNo: results.data[x].demographicNo,
							name: results.data[x].lastName + ',' + results.data[x].firstName
						});
					}
					return resp;
				},
				function error(errors)
				{
					console.log(errors);
				});
		};

		controller.searchProviders = function searchProviders(val)
		{
			var search = {
				searchTerm: val,
				active: true
			};
			return providerService.searchProviders(search, 0, 10).then(function(response)
			{
				var resp = [];
				for (var x = 0; x < response.length; x++)
				{
					resp.push(
					{
						providerNo: response[x].providerNo,
						name: response[x].firstName + ' ' + response[x].lastName
					});
				}
				return resp;
			});
		};

		controller.updateProviderNo = function updateProviderNo(item, model, label)
		{
			controller.tickler.taskAssignedTo = model;
			controller.tickler.taskAssignedToName = label;
		};

		controller.setSuggestedText = function setSuggestedText()
		{
			var results = $filter('filter')(controller.textSuggestions,
			{
				id: controller.tickler.suggestedTextId
			}, true);

			if (results != null)
			{
				controller.tickler.message = results[0].suggestedText;
			}
		};

		controller.addMonthsFromNow = function(num)
		{
			controller.tickler.serviceDateDate = moment().add(num, 'months').toDate();
		};

		controller.setTicklerProvider = async function setTicklerProvider()
		{
			try
			{
				let systemPrefApiResponse = await systemPreferenceApi.getPropertyValue("default_tickler_provider");
				controller.defaultTicklerProviderNo = parseInt(systemPrefApiResponse.data.body);

				if (systemPrefApiResponse.data.body !== null)
				{
					let providerServiceResponse = await providerService.getProvider(controller.defaultTicklerProviderNo);
					setTicklerProviderAssignee(providerServiceResponse);
				}
			}
			catch (error)
			{
				console.log(error);
			}
		}

		function setTicklerProviderAssignee(resp)
		{
			let firstName = resp.firstName || "";
			let lastName = resp.lastName || "";

			if (firstName === "" && lastName === "")
			{
				return;
			}

			let name = firstName + " " + lastName;
			controller.defaultTicklerProviderName = name;

			controller.tickler.taskAssignedTo = controller.defaultTicklerProviderNo;
			controller.tickler.taskAssignedToName = controller.defaultTicklerProviderName;
		}
	}
]);