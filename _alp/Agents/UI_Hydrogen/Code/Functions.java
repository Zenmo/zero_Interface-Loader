double f_styleHydrogenUI()
{/*ALCODESTART::1717506309797*/

/*ALCODEEND*/}

double f_initializeUIElectrolyser(ConnectionOwner COC)
{/*ALCODESTART::1717506309801*/
//Instantiate sliders 
f_instantiateSlidersElectrolyser(COC);
/*ALCODEEND*/}

double f_instantiateSlidersElectrolyser(ConnectionOwner COC)
{/*ALCODESTART::1717506309803*/
// Electrolyser Power start  value
v_sliderStartValue_ElectrolyserP_MW = ((J_EAConversionElectrolyser)COC.f_getOwnedGridConnections().get(0).c_conversionAssets.get(0)).getInputCapacity_kW()/1000; 
sl_electrolyserPower.setValue(v_sliderStartValue_ElectrolyserP_MW, false);
t_powerElectrolyser.setText(v_sliderStartValue_ElectrolyserP_MW + " MW");


// Price limit start value
v_sliderStartValue_priceLimit = ((GCEnergyConversion)COC.f_getOwnedGridConnections().get(0)).v_electricityPriceMaxForProfit_eurpkWh; 
sl_electrolyserElectricityPriceLimit.setValue(v_sliderStartValue_priceLimit, false);
t_electricityPriceLimit.setText("€" + v_sliderStartValue_priceLimit);


//Balance limit start value
v_sliderStartValue_balanceLimit = ((GCEnergyConversion)COC.f_getOwnedGridConnections().get(0)).v_gridNodeCongestionLimit_kW; 
sl_electrolyserElectricityBalanceLimit.setValue(v_sliderStartValue_balanceLimit, false);
t_electricityBalanceLimit.setText(v_sliderStartValue_balanceLimit + " MW");


//Radio button balance or price mode
int rb_value = 0;
switch (((GCEnergyConversion)COC.f_getOwnedGridConnections().get(0)).p_electrolyserOperationMode){

	case PRICE:
		
		rb_value = 0;
		//Set visibility of price slider true and balance slider false
		gr_electricityPriceSlider.setVisible(true);
		gr_electricityBalanceSlider.setVisible(false);
	break;
	
	case BALANCE:
		rb_value = 1;
		
		//Set visibility of balance slider true and price slider false
		gr_electricityBalanceSlider.setVisible(true);
		gr_electricityPriceSlider.setVisible(false);
	break;
}
rb_electrolyserMode.setValue(rb_value, false);
/*ALCODEEND*/}

double f_setScenarioFuture(GridConnection GC)
{/*ALCODESTART::1724859526072*/
//Set button to custom early on, so traceln will get ignored.
rb_scenariosPrivateUI.setValue(2, false);


//Scenario code here


//Reset button to current, due to sliders putting it on custom
rb_scenariosPrivateUI.setValue(1, false);
/*ALCODEEND*/}

double f_setScenario(int scenario_nr)
{/*ALCODESTART::1724859526076*/
switch (scenario_nr){

	case 0: // Current

	break;
	
	case 1: // Future

	break;
	
	case 2: // Hydrogen
		traceln("Selected scenario: Hydrogen : DOES NOTHING YET");
	break;
	
	case 3: // Custom

		if(rb_scenariosPrivateUI.getValue() == 2){
		return;
		}
		rb_scenariosPrivateUI.setValue(2, false);
		traceln("Selected scenario: Custom");
	break;
	
	default:

}

//Set 'results up to date' to false
zero_Interface.b_resultsUpToDate = false;
/*ALCODEEND*/}

double f_setScenarioCurrent(GridConnection GC)
{/*ALCODESTART::1724859526078*/
//Set button to custom early on, so traceln will get ignored.
rb_scenariosPrivateUI.setValue(2, false);


//Scenario code here


//Reset button to current, due to sliders putting it on custom
rb_scenariosPrivateUI.setValue(0, false);
/*ALCODEEND*/}

double f_setSelectedGC()
{/*ALCODESTART::1724859526080*/
//Initialize slider presets to selected GC (min, max, etc.)
//f_setSliderPresets();


/*ALCODEEND*/}

