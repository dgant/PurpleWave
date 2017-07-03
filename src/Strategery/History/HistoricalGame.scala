package Strategery.History

case class HistoricalGame(
  id            : Int,
  mapName       : String,
  opponentName  : String,
  won           : Boolean,
  strategies    : Set[String])
