<juno-simple-modal class="add-queued-appointment-modal"
									 component-style="$ctrl.resolve.style"
									 modal-instance="$ctrl.modalInstance">

	<div class="content">
		<div ng-if="!$ctrl.isLoading">
			<h5 class="title juno-text-primary">Move Queued Appointment to Schedule </h5>

			<p class="juno-text">
				Select which schedule you want to move the queued appointment to.
			</p>

			<div class="center-options flex-row align-items-flex-end">
				<juno-typeahead
								name="Provider"
								model="$ctrl.bookProviderNo"
								options="$ctrl.providerOptions"
								placeholder="Assign to provider"
								component-style="$ctrl.resolve.style"
								on-selected="$ctrl.onProviderSelect()">
				</juno-typeahead>

				<juno-button ng-click="$ctrl.assignToMe()"
										 component-style="$ctrl.resolve.style"
										 button-color="$ctrl.JUNO_BUTTON_COLOR.PRIMARY"
										 button-color-pattern="$ctrl.JUNO_BUTTON_COLOR_PATTERN.FILL">
					Assign To Me
				</juno-button>
			</div>
		</div>
		<juno-loading-indicator ng-if="$ctrl.isLoading"
														class="loading-indicator-container"
														message = "Scheduling..."
														message-alignment="vertical"
														indicator-type="dot-pulse">
		</juno-loading-indicator>
	</div>

	<juno-divider component-style="$ctrl.resolve.style"
								slim="true">
	</juno-divider>

	<div class="buttons">
		<juno-button ng-click="$ctrl.onCancel()"
								 component-style="$ctrl.resolve.style"
								 button-color="$ctrl.JUNO_BUTTON_COLOR.GREYSCALE_LIGHT"
								 button-color-pattern="$ctrl.JUNO_BUTTON_COLOR_PATTERN.FILL">
			Cancel
		</juno-button>
		<juno-button ng-click="$ctrl.bookQueuedAppointment()"
								 title="{{$ctrl.bookTooltip('Assign the appointment to a schedule')}}"
								 component-style="$ctrl.resolve.style"
								 button-color="$ctrl.JUNO_BUTTON_COLOR.PRIMARY"
								 button-color-pattern="$ctrl.JUNO_BUTTON_COLOR_PATTERN.DEFAULT"
								 disabled="!$ctrl.bookProviderNo || $ctrl.isLoading || !$ctrl.providerHasSite">
			Assign
		</juno-button>
		<juno-button ng-click="$ctrl.bookAndStartTelehealth()"
								 title="{{$ctrl.bookTooltip('Assign the appointment to a schedule and start the telehealth call')}}"
								 component-style="$ctrl.resolve.style"
								 button-color="$ctrl.JUNO_BUTTON_COLOR.PRIMARY"
								 button-color-pattern="$ctrl.JUNO_BUTTON_COLOR_PATTERN.FILL"
								 disabled="!$ctrl.bookProviderNo || $ctrl.isLoading || !$ctrl.providerHasSite || !$ctrl.resolve.isVirtual">
			Start
		</juno-button>
	</div>

</juno-simple-modal>