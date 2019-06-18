package Strategery.Strategies.Utility

import java.io.{BufferedWriter, File, FileWriter}

import Lifecycle.With
import Planning.Plan
import Strategery.Strategies.Strategy

object EvELogMapInfo extends Strategy {
  override def gameplan: Option[Plan] = Some(new Plan {
    override def onUpdate(): Unit = {
      val content = With.geography.startBases.map(base => "Base " + base.townHallTile + " -> " + base.natural.map(_.townHallTile).getOrElse("NONE")).mkString("\n")
      val filename = With.mapFileName + ".info"
      val file            = new File(filename)
      val fileWriter      = new FileWriter(file)
      val bufferedWriter  = new BufferedWriter(fileWriter)
      bufferedWriter.write(content)
      bufferedWriter.close()
      With.game.leaveGame()
    }
  })
}
