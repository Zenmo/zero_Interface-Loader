# zero_Interface-Loader

### Before use:
The Interface-Loader .alp file uses Lombok (https://projectlombok.org/) to create data packages. At https://zerodocs.zenmo.com/ under the chapter: Lombok in AnyLogic, you can find the details to make this work within AnyLogic. The Lombok.jar file is also already in this repository.


### Important No(te)s
This repository functions as a library. It should be extended by a project specific model which contains a child loader and child interface together with a startUp agent that functions as the main. 
This means that this repository does not have to and should not contain and project specific data or functions. In other words:
- No project names
- No (non generic) data
- No functions that are specific for a single project
- No images (other than the basic logos and interface images)


## Structure

### Loader
The basic purpose of the loader is to configure the engine and interface.

The loader contains collections that should be filled by (lombok) packages created by the project specific child loader.
These collections are used by the loader functions to create the agent populations that are used in the simulations.
The loader has a default load in structure for two project types: BUSINESSPARK or RESIDENTIAL. 
If you want to deviate from this structure, you can override and add functions to your project specific child loader. But again: no adding to this repository!



### Interface
The interface contains all the visual aspects of the model. This includes the GIS map, sliders, scenario buttons, legenda, etc.
The resultsUI.alp is configured by and connected to the interface as well, which shows all the graphs and KPIs of the model.
By overriding certain functions (f_projectSpecificSettings, f_projectSpecificStyling, etc.) in the project specific child interface, one can set variables, colors and other settings to whatever way they like.
  
