package Strategery.History

case class HistoricalGame(
  timestamp  : Long,
  mapName    : String,
  enemyName  : String,
  won        : Boolean,
  strategies : Set[String])
