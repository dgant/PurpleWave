package Information.Battles.Types

trait JudgmentModifier {
  def name                      : String = getClass.getSimpleName.replace("$", "")
  def applies(to: BattleLocal)  : Boolean = true
  def gainedValueMultiplier     : Double
  def friendlyMovementMultplier : Double
  def friendlyAttackMultiplier  : Double
  def enemyMovementMultplier    : Double
  def enemyAttackMultiplier     : Double
  def patienceMultiplier        : Double
  def moraleMultiplier          : Double
  def confidenceMultiplier      : Double
}
