package Startup

import bwapi.{BWEventListener, Player}

object Listener extends BWEventListener{
  val mirror:bwapi.Mirror = new bwapi.Mirror()
  var bot:Option[Bot] = None

  def initialize(): Unit = {
    mirror.getModule.setEventListener(this)
    mirror.startGame
  }

  override def onStart(): Unit = {
    With.game = mirror.getGame
    bot = Some(new Bot())
    var foo = 3
    var bar = 5
    var qux = foo * bar
    bot.get.onStart
  }

  override def onEnd(b: Boolean):                         Unit = { bot.get.onEnd(b) }
  override def onFrame():                                 Unit = { bot.get.onFrame }
  override def onSendText(s: String):                     Unit = { bot.get.onSendText(s) }
  override def onReceiveText(player: Player, s: String):  Unit = { bot.get.onReceiveText(player, s) }
  override def onPlayerLeft(player: Player):              Unit = { bot.get.onPlayerLeft(player) }
  override def onPlayerDropped(player: Player):           Unit = { bot.get.onPlayerDropped(player) }
  override def onNukeDetect(position: bwapi.Position):    Unit = { bot.get.onNukeDetect(position) }
  override def onUnitComplete(unit: bwapi.Unit):          Unit = { bot.get.onUnitComplete(unit) }
  override def onUnitCreate(unit: bwapi.Unit):            Unit = { bot.get.onUnitCreate(unit) }
  override def onUnitDestroy(unit: bwapi.Unit):           Unit = { bot.get.onUnitDestroy(unit) }
  override def onUnitDiscover(unit: bwapi.Unit):          Unit = { bot.get.onUnitDiscover(unit) }
  override def onUnitEvade(unit: bwapi.Unit):             Unit = { bot.get.onUnitEvade(unit) }
  override def onUnitHide(unit: bwapi.Unit):              Unit = { bot.get.onUnitHide(unit) }
  override def onUnitMorph(unit: bwapi.Unit):             Unit = { bot.get.onUnitMorph(unit) }
  override def onUnitRenegade(unit: bwapi.Unit):          Unit = { bot.get.onUnitRenegade(unit) }
  override def onUnitShow(unit: bwapi.Unit):              Unit = { bot.get.onUnitShow(unit) }
  override def onSaveGame(s: String):                     Unit = { bot.get.onSaveGame(s) }
}
