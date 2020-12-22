package Information.Battles.Types

case class JudgmentModifier(
  var name                  : String,
  gainedValueMultiplier     : Double = 0,
  friendlyMovementMultplier : Double = 0,
  friendlyAttackMultiplier  : Double = 0,
  enemyMovementMultplier    : Double = 0,
  enemyAttackMultiplier     : Double = 0,
  patienceMultiplier        : Double = 0,
  moraleMultiplier          : Double = 0,
  confidenceMultiplier      : Double = 0,
  targetDelta               : Double = 0)
