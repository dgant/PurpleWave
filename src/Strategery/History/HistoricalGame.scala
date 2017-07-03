package Strategery.History

case class HistoricalGame(
                           id            : Int,
                           mapName       : String,
                           enemyName  : String,
                           won           : Boolean,
                           strategies    : Set[String])
