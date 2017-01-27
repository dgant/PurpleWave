package Startup

import bwapi.DefaultBWListener
import bwta.BWTA

class Bot(var game:bwapi.Game) extends DefaultBWListener {
  val self = game.self

  override def onStart(): Unit = {
    System.out.println("Purple Wave, reporting in.");
    System.out.println("Reading map");
    BWTA.readMap();

    System.out.println("Analyzing map");
    BWTA.analyze();

    System.out.println("Initialization complete");
  }

  override def onFrame(): Unit = {

  }
}
