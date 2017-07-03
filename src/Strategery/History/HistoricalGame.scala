package Strategery.History

import bwapi.Race

case class HistoricalGame(
  timestamp   : Long,
  mapName     : String,
  enemyName   : String,
  ourRace     : Race,
  enemyRace   : Race,
  won         : Boolean,
  strategies  : Set[String])
