package Debugging

import Lifecycle.With
import Mathematics.Maff

import java.io.{File, PrintWriter}

object WriteMapCSV {

  def apply(): Unit = {
    if (With.frame > 0) return

    var output = ""
    def append(values: Any*): Unit = {
      output += values.map(_.toString).mkString("\t") + "\r\n"
    }

    val startMetros       = With.geography.metros.filter(_.main.isDefined)
    val startMetroSizeMin = Maff.min(startMetros.map(_.bases.length)).getOrElse(0)
    val startMetroSizeMax = Maff.max(startMetros.map(_.bases.length)).getOrElse(0)
    val airlocksMin       = Maff.min(startMetros.map(_.airlocks.size)).getOrElse(0)
    val airlocksMax       = Maff.max(startMetros.map(_.airlocks.size)).getOrElse(0)
    val exitWidths        = With.geography.startBases.flatten(_.zone.exitOriginal.map(_.diameterPixels))

    append("File",                    With.mapFileName)
    append("Name",                    With.game.mapName)
    append("Hash",                    With.game.mapHash)
    append("Width",                   With.game.mapWidth)
    append("Height",                  With.game.mapHeight)
    append("Ramped?",                 With.strategy.isRamped)
    append("Flat?",                   With.strategy.isFlat)
    append("Inverted?",               With.strategy.isInverted)
    append("Height @ Main",           With.strategy.heightMain)
    append("Height @ Natural",        With.strategy.heightNatural)
    append("Exit width min",          Maff.min(exitWidths).getOrElse(0))
    append("Exit width max",          Maff.max(exitWidths).getOrElse(0))
    append("Bases",                   With.geography.bases.length)
    append("Starts",                  With.geography.bases.count(_.isStartLocation))
    append("Naturals",                With.geography.bases.count(_.naturalOf.isDefined))
    append("Islands",                 With.geography.bases.count(_.island))
    append("Pockets",                 With.geography.bases.count(_.isPocket))
    append("Backyards",               With.geography.bases.count(_.isBackyard))
    append("Main metro size min",     startMetroSizeMin)
    append("Main metro size max",     startMetroSizeMax)
    append("Main metro sizes match",  startMetroSizeMin == startMetroSizeMax)
    append("Airlocks min",            airlocksMin)
    append("Airlocks max",            airlocksMax)
    append("Airlocks match",          airlocksMin == airlocksMax)
    append("Rush distances",          With.geography.rushDistances.mkString(", "))
    append("Rush distance min",       With.strategy.rushDistanceMin)
    append("Rush distance max",       With.strategy.rushDistanceMax)
    append("Rush distance mean",      With.strategy.rushDistanceMean)
    append("Metro shared zones",      With.geography.zones.count(z => With.geography.metros.count(_.zones.contains(z)) > 1))
    append("Metros")
    append(With.geography.metros: _*)
    append("Bases")
    append(With.geography.bases: _*)

    val filename = f"${With.bwapiData.write}map-${With.mapFileName}.mapinfo.txt"
    val file = new File(filename)
    val printWriter = new PrintWriter(file)
    printWriter.write(output)
    printWriter.close()
  }
}
