package com.boarbeard.generator.beimax;

import com.boarbeard.generator.beimax.event.DataTransfer;
import com.boarbeard.generator.beimax.event.IncomingData;
import com.boarbeard.generator.beimax.event.Threat;


public class ConstructedMissions {

	public static EventList firstTestRun() {		
		EventList eventList = new EventList();		
		eventList.addPhaseEvents(255, 170);
		
		//Phase 1
		eventList.addEvent(15, threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(60, threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(90, new DataTransfer());
		eventList.addEvent(135, threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(200, new DataTransfer());

		//Phase 2
		eventList.addEvent(280, new IncomingData());
		eventList.addEvent(310, new DataTransfer());
		
		return eventList;
	}
	
	public static EventList secondTestRun() {		
		EventList eventList = new EventList();		
		eventList.addPhaseEvents(225, 200);
		
		//Phase 1
		eventList.addEvent(10, threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(50, new IncomingData());
		eventList.addEvent(80, threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(135, new DataTransfer());

		//Phase 2
		eventList.addEvent(225, threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addEvent(290, new DataTransfer());
		eventList.addEvent(330, new IncomingData());
		
		return eventList;
	}
	
	public static EventList simulation1() {
		EventList eventList = new EventList();		
		eventList.addPhaseEvents(225, 230, 145);

		//Phase 1
		eventList.addEvent(10, threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(70, new IncomingData());
		eventList.addEvent(90, threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS, 
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(120, new DataTransfer());
		eventList.addEvent(170, new DataTransfer());

		//Phase 2
		eventList.addEvent(230, threat(false, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addEvent(290, threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS, 
				Threat.THREAT_POSITION_EXTERNAL, 6));		
		eventList.addEvent(340, new DataTransfer());
		eventList.addWhiteNoiseEvents(360, 15);
		eventList.addEvent(405, new IncomingData());
		
		//Phase 3
		eventList.addWhiteNoiseEvents(470, 10);
		eventList.addEvent(505, new DataTransfer());
		
		return eventList;
	}
	
	public static EventList simulation2() {
		EventList eventList = new EventList();		
		eventList.addPhaseEvents(225, 230, 150);

		//Phase 1
		eventList.addEvent(10, new IncomingData());		
		eventList.addEvent(20, threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS, 
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(70, new DataTransfer());
		eventList.addEvent(100, threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addEvent(180, new DataTransfer());

		//Phase 2
		eventList.addWhiteNoiseEvents(230, 10);
		eventList.addEvent(250, threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_SERIOUS, 
				Threat.THREAT_POSITION_EXTERNAL, 6));
		eventList.addEvent(285, new IncomingData());
		eventList.addWhiteNoiseEvents(300, 10);
		eventList.addEvent(330, threat(false, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL, 
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(360, new DataTransfer());

		//Phase 3
		eventList.addEvent(480, new DataTransfer());
		eventList.addWhiteNoiseEvents(520, 10);
		
		return eventList;
	}
	
	public static EventList simulation3() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 55);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 1));		
		eventList.addEvent(sec(1, 05), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(sec(1, 40), new IncomingData());
		eventList.addWhiteNoiseEvents(sec(2, 00), 10);
		eventList.addEvent(sec(2, 30), new DataTransfer());
		eventList.addEvent(sec(3, 05), threat(false, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		
		//Phase 2
		eventList.addEvent(sec(4, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addEvent(sec(4, 40), new DataTransfer());
		eventList.addEvent(sec(5, 00), new IncomingData());
		eventList.addEvent(sec(5, 20), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(5, 55), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(6, 40), 10);
		
		//Phase 3
		eventList.addWhiteNoiseEvents(sec(7, 50), 15);
		eventList.addEvent(sec(8, 10), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 25), 5);
		
		return eventList;
	}
	
	public static EventList advancedsimulation1() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 55);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 2));
		eventList.addEvent(sec(1, 00), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(sec(1, 50), new DataTransfer());
		eventList.addEvent(sec(2, 20), threat(false, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addEvent(sec(3, 10), new IncomingData());
		
		//Phase 2
		eventList.addEvent(sec(4, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 5));
		eventList.addEvent(sec(4, 50), new IncomingData());
		eventList.addEvent(sec(5, 20), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(5, 40), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(6, 00), 10);
		eventList.addEvent(sec(6, 40), new DataTransfer());
		
		//Phase 3
		eventList.addWhiteNoiseEvents(sec(7, 50), 20);
		eventList.addEvent(sec(8, 20), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(9, 10), 10);
		
		return eventList;
	}
	
	public static EventList advancedsimulation2() {
		EventList eventList = new EventList();
		int firstPhase = sec(4, 05);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), new IncomingData());
		eventList.addEvent(sec(0, 20), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(sec(1, 15), threat(false, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addWhiteNoiseEvents(sec(2, 15), 15);
		eventList.addEvent(sec(2, 35), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_INTERNAL, 4));
		eventList.addEvent(sec(3, 20), new DataTransfer());
		
		//Phase 2
		eventList.addEvent(sec(4, 20), new IncomingData());
		eventList.addEvent(sec(4, 30), new DataTransfer());
		eventList.addEvent(sec(4, 45), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addWhiteNoiseEvents(sec(5, 20), 30);
		
		//Phase 3
		eventList.addEvent(sec(7, 35), new DataTransfer());
		eventList.addEvent(sec(8, 00), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 30), 10);
		
		return eventList;
	}
	
	public static EventList advancedsimulation3() {
		EventList eventList = new EventList();
		int firstPhase = sec(4, 05);
		int secondPhase = sec(7, 35);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(sec(1, 10), new IncomingData());
		eventList.addEvent(sec(1, 40), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_INTERNAL, 3));
		eventList.addEvent(sec(2, 30), new DataTransfer());
		eventList.addEvent(sec(3, 20), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		
		//Phase2
		eventList.addEvent(sec(4, 20), threat(false, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addEvent(sec(5, 00), new IncomingData());
		eventList.addEvent(sec(5, 20), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 6));
		eventList.addWhiteNoiseEvents(sec(5, 45), 10);
		eventList.addEvent(sec(6, 05), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(6, 45), 15);
		
		//Phase3
		eventList.addWhiteNoiseEvents(sec(7, 50), 10);
		eventList.addWhiteNoiseEvents(sec(8, 05), 10);
		eventList.addEvent(sec(9, 05), new DataTransfer());
		
		return eventList;
	}
	
	public static EventList realmission1() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 40);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(sec(0, 55), threat(false, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 3));
		eventList.addEvent(sec(1, 50), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addEvent(sec(2, 20), new IncomingData());
		eventList.addWhiteNoiseEvents(sec(2, 50), 10);
		eventList.addEvent(sec(3, 05), new DataTransfer());
		
		//Phase 2
		eventList.addEvent(sec(3, 50), new IncomingData());
		eventList.addEvent(sec(4, 00), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 5));
		eventList.addEvent(sec(4, 25), new DataTransfer());
		eventList.addEvent(sec(4, 50), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 6));
		eventList.addWhiteNoiseEvents(sec(5, 20), 15);
		eventList.addEvent(sec(5, 50), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(6, 35), new DataTransfer());
		
		//Phase 3
		eventList.addWhiteNoiseEvents(sec(7, 50), 20);
		eventList.addEvent(sec(8, 20), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(9, 15), 10);
		
		return eventList;
	}
	
	public static EventList realmission2() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 55);
		int secondPhase = sec(7, 40);
		int thirdPhase = sec(10, 15);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(false, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(sec(0, 35), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 2));
		eventList.addEvent(sec(1, 30), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(sec(2, 00), new DataTransfer());
		eventList.addEvent(sec(2, 35), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addEvent(sec(3, 10), new IncomingData());
		
		//Phase 2
		eventList.addEvent(sec(4, 05), new IncomingData());
		eventList.addEvent(sec(4, 15), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addEvent(sec(4, 50), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_INTERNAL, 6));
		eventList.addWhiteNoiseEvents(sec(5, 20), 20);
		eventList.addEvent(sec(5, 45), new DataTransfer());
		eventList.addEvent(sec(6, 15), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 8));
		eventList.addEvent(sec(7, 05), new IncomingData());
		
		//Phase 3
		eventList.addEvent(sec(8, 00), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 20), 30);
		eventList.addEvent(sec(9, 25), new DataTransfer());
		
		return eventList;
	}
	
	public static EventList realmission3() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 45);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 2));
		eventList.addEvent(sec(0, 50), new IncomingData());
		eventList.addEvent(sec(1, 15), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addWhiteNoiseEvents(sec(1, 50), 10);
		eventList.addEvent(sec(2, 15), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 4));
		eventList.addEvent(sec(3, 05), new DataTransfer());
		
		//Phase 2
		eventList.addEvent(sec(4, 00), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 6));
		eventList.addWhiteNoiseEvents(sec(4, 30), 20);
		eventList.addEvent(sec(4, 55), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(5, 20), new DataTransfer());
		eventList.addEvent(sec(5, 40), new IncomingData());
		eventList.addEvent(sec(5, 55), threat(false, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 8));
		eventList.addEvent(sec(6, 50), new DataTransfer());
		
		//Phase 3
		eventList.addWhiteNoiseEvents(sec(7, 40), 20);
		eventList.addEvent(sec(8, 05), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 15), 10);
		
		return eventList;
	}
	
	public static EventList realmission4() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 40);
		int secondPhase = sec(7, 15);
		int thirdPhase = sec(9, 35);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(sec(1, 00), threat(false, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(sec(1, 30), new IncomingData());
		eventList.addEvent(sec(1, 55), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addEvent(sec(2, 25), new DataTransfer());
		
		//Phase 2
		eventList.addEvent(sec(3, 55), new DataTransfer());
		eventList.addEvent(sec(4, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_INTERNAL, 5));
		eventList.addEvent(sec(4, 35), new IncomingData());
		eventList.addEvent(sec(5, 00), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 6));
		eventList.addWhiteNoiseEvents(sec(5, 45), 10);
		eventList.addEvent(sec(6, 25), new IncomingData());
		eventList.addWhiteNoiseEvents(sec(6, 35), 10);
		
		//Phase 3
		eventList.addEvent(sec(7, 35), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 00), 20);
		eventList.addWhiteNoiseEvents(sec(8, 55), 15);
		
		return eventList;
	}
	
	public static EventList realmission5() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 45);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addWhiteNoiseEvents(sec(0, 10), 5);
		eventList.addEvent(sec(0, 20), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 2));
		eventList.addEvent(sec(1, 05), new IncomingData());
		eventList.addEvent(sec(1, 30), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_INTERNAL, 3));
		eventList.addEvent(sec(2, 20), new DataTransfer());
		eventList.addEvent(sec(2, 55), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		
		//Phase 2
		eventList.addWhiteNoiseEvents(sec(4, 00), 25);
		eventList.addEvent(sec(4, 30), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 6));
		eventList.addEvent(sec(5, 05), threat(false, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 7));
		eventList.addEvent(sec(6, 00), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 8));
		eventList.addEvent(sec(6, 35), new DataTransfer());
		eventList.addEvent(sec(6, 50), new DataTransfer());
		
		//Phase 3
		eventList.addEvent(sec(7, 45), new IncomingData());
		eventList.addEvent(sec(8, 00), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 20), 10);
		
		return eventList;
	}
	
	public static EventList realmission6() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 45);
		int secondPhase = sec(7, 40);
		int thirdPhase = sec(10, 15);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), new IncomingData());
		eventList.addEvent(sec(0, 20), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(sec(0, 45), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 2));
		eventList.addEvent(sec(1, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addWhiteNoiseEvents(sec(1, 30), 10);
		eventList.addEvent(sec(2, 10), new IncomingData());
		eventList.addEvent(sec(3, 00), new IncomingData());
		
		//Phase 2
		eventList.addEvent(sec(3, 55), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addEvent(sec(4, 25), new DataTransfer());
		eventList.addEvent(sec(4, 45), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 6));
		eventList.addEvent(sec(5, 20), threat(false, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(6, 05), new DataTransfer());
		eventList.addEvent(sec(6, 50), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 8));
		
		//Phase 3
		eventList.addWhiteNoiseEvents(sec(8, 00), 30);
		eventList.addWhiteNoiseEvents(sec(8, 40), 5);
		eventList.addEvent(sec(9, 40), new DataTransfer());
		
		return eventList;
	}
	
	public static EventList realmission7() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 35);
		int secondPhase = sec(7, 25);
		int thirdPhase = sec(9, 55);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(false, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 1));
		eventList.addEvent(sec(0, 35), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 3));
		eventList.addEvent(sec(1, 10), new IncomingData());
		eventList.addEvent(sec(1, 45), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_INTERNAL, 4));
		eventList.addEvent(sec(2, 15), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(2, 55), 10);
		
		//Phase 2
		eventList.addEvent(sec(3, 45), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addEvent(sec(4, 05), new IncomingData());
		eventList.addEvent(sec(4, 25), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(4, 50), new DataTransfer());
		eventList.addEvent(sec(5, 20), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 8));
		eventList.addEvent(sec(6, 00), new DataTransfer());
		
		//Phase 3
		eventList.addEvent(sec(7, 35), new IncomingData());
		eventList.addWhiteNoiseEvents(sec(7, 55), 05);
		eventList.addWhiteNoiseEvents(sec(8, 05), 10);
		eventList.addWhiteNoiseEvents(sec(8, 20), 25);
		
		return eventList;
	}
	
	public static EventList realmission8() {
		EventList eventList = new EventList();
		int firstPhase = sec(3, 20);
		int secondPhase = sec(7, 10);
		int thirdPhase = sec(9, 35);
		eventList.addPhaseEvents(firstPhase + 10, secondPhase - firstPhase, thirdPhase - secondPhase);
		
		//Phase 1
		eventList.addEvent(sec(0, 10), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_INTERNAL, 3));
		eventList.addWhiteNoiseEvents(sec(0, 40), 10);
		eventList.addEvent(sec(1, 10), threat(true, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 4));
		eventList.addWhiteNoiseEvents(sec(1, 30), 15);
		eventList.addEvent(sec(2, 30), new IncomingData());
		eventList.addEvent(sec(2, 40), new IncomingData());
		
		//Phase 2
		eventList.addEvent(sec(3, 30), threat(true, Threat.THREAT_SECTOR_WHITE, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 5));
		eventList.addWhiteNoiseEvents(sec(4, 00), 10);
		eventList.addEvent(sec(4, 35), new DataTransfer());
		eventList.addEvent(sec(4, 55), threat(true, Threat.THREAT_SECTOR_RED, Threat.THREAT_LEVEL_SERIOUS,
				Threat.THREAT_POSITION_EXTERNAL, 7));
		eventList.addEvent(sec(5, 20), new DataTransfer());
		eventList.addEvent(sec(6, 20), threat(false, Threat.THREAT_SECTOR_BLUE, Threat.THREAT_LEVEL_NORMAL,
				Threat.THREAT_POSITION_EXTERNAL, 8));
		
		//Phase 3
		eventList.addWhiteNoiseEvents(sec(7, 30), 10);
		eventList.addEvent(sec(8, 10), new DataTransfer());
		eventList.addWhiteNoiseEvents(sec(8, 40), 20);
		
		return eventList;
	}
	
	private static int sec(int minutes, int seconds) {
		
		return (minutes * 60 + seconds);
		
	}
	
	private static Threat threat(boolean confirmed, int sector, int threatLevel,
			int threatPosition, int time) {
		Threat threat = new Threat();
		threat.setConfirmed(confirmed);
		threat.setSector(sector);
		threat.setThreatLevel(threatLevel);
		threat.setThreatPosition(threatPosition);
		threat.setTime(time);
		return threat;
	}
}
