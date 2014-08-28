Glucometer
==========

Project to be used with Eclipse

Some components have been cloned from project 'GNU Gluco Control'

The goal is to handle glucose meters outside of GGC application

Java application managing some Onetouch glucometers :
-----------------------------------------------------

Supported meters :

1 - Onetouch Ultra : This glucose meter requires to be connected via a special cable, making the link between an USB port, and the meter interface presenting a RS232 on a jack connector. So, the cable is seen as a PL2303 interface by PC

  The 'ultra' class of the project is fully functionnal : connect to device, get the reader's serial number, and read all records from meter's memory
  
2 - Onetouch Verio IQ : This meter is connected to PC via a normal USB cable : A -> mini USB. The device itself includes a CP2103 cuircuit, making adaptation between USB host, and internal TTL interface. So, the meter is seen as a CP2103 interface by PC (RS232).
In actual state of project, the 'Verio' class does'nt work, because the protocol used by this reader is undocumented, and requires retro-engineering

To generate/use this application :
- Create a work directory, by example 'Glucometer'
-      In this directory, you can use Eclipse to generate a 'Runnable jar file', by example 'Glucometer.jar'

This directory must also contain :

-     - ls_ot_ultra.jpg ( image for ultra reader )
-     - ls_ot_verio.jpg ( image for verio reader )
-     - eng.xml ( definitions for english language )
-     - fr.xml ( definitions for french language )
-     - settings.xml ( a file defining all settings for session, configuration, language to use, ... )
 
you can export those files from your eclipse environment, subdirectory 'misc'
 
For now, no GUI tool is included to configure 'settings.xml' : It has to be configured manually, with an editor, before use of application
