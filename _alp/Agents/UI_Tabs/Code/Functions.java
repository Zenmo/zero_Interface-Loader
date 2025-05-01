double f_showCorrectTab()
{/*ALCODESTART::1722245473562*/
pop_tabElectricity_presentation.setVisible(false);
pop_tabHeating_presentation.setVisible(false);
pop_tabMobility_presentation.setVisible(false);
pop_tabEHub_presentation.setVisible(false);

switch (v_selectedTabType) {
	case ELECTRICITY:
		pop_tabElectricity_presentation.setVisible(true);
		//p_customPopPresentation.setVisible(true);
		break;
	case HEAT:
		pop_tabHeating_presentation.setVisible(true);
		break;
	case MOBILITY:
		pop_tabMobility_presentation.setVisible(true);
		break;
	case HUB:
	case NFATO:
		pop_tabEHub_presentation.setVisible(true);
		//tabEHub.f_setTab();
		break;
}

/*ALCODEEND*/}

double f_setTab(EnergyDemandTab tab)
{/*ALCODESTART::1722259092945*/
v_selectedTabType = tab;
f_showCorrectTab();

/*ALCODEEND*/}

