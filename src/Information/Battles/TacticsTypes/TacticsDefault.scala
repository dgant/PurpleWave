package Information.Battles.TacticsTypes

object TacticsDefault {
  def get:TacticsOptions = {
    val output = new TacticsOptions()
    output.add(Tactics.Movement.Charge)
    output.add(Tactics.Focus.None)
    output.add(Tactics.Wounded.Fight)
    output.add(Tactics.Workers.Ignore)
    output
  }
}
