package Startup


import bwapi.{DefaultBWListener, Player}

object Main extends DefaultBWListener {
  val _mirror:bwapi.Mirror = new bwapi.Mirror()
  var _bot:Option[DefaultBWListener] = None

  def main(args: Array[String]) {
    System.out.println("main")
    _mirror.getModule.setEventListener(this)
    System.out.println("listener on")
    _mirror.startGame
  }

  override def onStart(): Unit = {
    System.out.println("onStart")
    _bot = Some(new Bot(_mirror.getGame))
    _bot.get.onStart
  }

  override def onEnd(b: Boolean):                         Unit = { _bot.get.onEnd(b) }
  override def onFrame():                                 Unit = { _bot.get.onFrame }
  override def onSendText(s: String):                     Unit = { _bot.get.onSendText(s) }
  override def onReceiveText(player: Player, s: String):  Unit = { _bot.get.onReceiveText(player, s) }
  override def onPlayerLeft(player: Player):              Unit = { _bot.get.onPlayerLeft(player) }
  override def onPlayerDropped(player: Player):           Unit = { _bot.get.onPlayerDropped(player) }
  override def onNukeDetect(position: bwapi.Position):    Unit = { _bot.get.onNukeDetect(position) }
  override def onUnitComplete(unit: bwapi.Unit):          Unit = { _bot.get.onUnitComplete(unit) }
  override def onUnitCreate(unit: bwapi.Unit):            Unit = { _bot.get.onUnitCreate(unit) }
  override def onUnitDestroy(unit: bwapi.Unit):           Unit = { _bot.get.onUnitDestroy(unit) }
  override def onUnitDiscover(unit: bwapi.Unit):          Unit = { _bot.get.onUnitDiscover(unit) }
  override def onUnitEvade(unit: bwapi.Unit):             Unit = { _bot.get.onUnitEvade(unit) }
  override def onUnitHide(unit: bwapi.Unit):              Unit = { _bot.get.onUnitHide(unit) }
  override def onUnitMorph(unit: bwapi.Unit):             Unit = { _bot.get.onUnitMorph(unit) }
  override def onUnitRenegade(unit: bwapi.Unit):          Unit = { _bot.get.onUnitRenegade(unit) }
  override def onUnitShow(unit: bwapi.Unit):              Unit = { _bot.get.onUnitShow(unit) }
  override def onSaveGame(s: String):                     Unit = { _bot.get.onSaveGame(s) }
}
