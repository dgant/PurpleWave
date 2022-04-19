package Lifecycle

import bwapi.{BWEventListener, Player}

object PurpleEventListener extends BWEventListener {

  var bot: Option[BWEventListener] = None

  var lastException: Option[Exception] = None
  var lastStackTrace: Option[String] = None
  var gameCount: Int = 0

  override def onStart(): Unit = {
    try {
      gameCount += 1
      bot = Some(new PurpleWave)
      bot.foreach(_.onStart())
    }
    catch { case exception: Exception =>
      lastException = Some(exception)
      lastStackTrace = Some(exception.getStackTrace.toString)
    }
  }

  override def onEnd(b: Boolean):                         Unit = { bot.foreach(_.onEnd(b)) }
  override def onFrame():                                 Unit = { bot.foreach(_.onFrame()) }
  override def onSendText(s: String):                     Unit = { bot.foreach(_.onSendText(s)) }
  override def onReceiveText(player: Player, s: String):  Unit = { bot.foreach(_.onReceiveText(player, s)) }
  override def onPlayerLeft(player: Player):              Unit = { bot.foreach(_.onPlayerLeft(player)) }
  override def onPlayerDropped(player: Player):           Unit = { bot.foreach(_.onPlayerDropped(player)) }
  override def onNukeDetect(position: bwapi.Position):    Unit = { bot.foreach(_.onNukeDetect(position)) }
  override def onUnitComplete(unit: bwapi.Unit):          Unit = { bot.foreach(_.onUnitComplete(unit)) }
  override def onUnitCreate(unit: bwapi.Unit):            Unit = { bot.foreach(_.onUnitCreate(unit)) }
  override def onUnitDestroy(unit: bwapi.Unit):           Unit = { bot.foreach(_.onUnitDestroy(unit)) }
  override def onUnitDiscover(unit: bwapi.Unit):          Unit = { bot.foreach(_.onUnitDiscover(unit)) }
  override def onUnitEvade(unit: bwapi.Unit):             Unit = { bot.foreach(_.onUnitEvade(unit)) }
  override def onUnitHide(unit: bwapi.Unit):              Unit = { bot.foreach(_.onUnitHide(unit)) }
  override def onUnitMorph(unit: bwapi.Unit):             Unit = { bot.foreach(_.onUnitMorph(unit)) }
  override def onUnitRenegade(unit: bwapi.Unit):          Unit = { bot.foreach(_.onUnitRenegade(unit)) }
  override def onUnitShow(unit: bwapi.Unit):              Unit = { bot.foreach(_.onUnitShow(unit)) }
  override def onSaveGame(s: String):                     Unit = { bot.foreach(_.onSaveGame(s)) }
}
