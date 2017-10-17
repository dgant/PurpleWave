package Planning.Plans.GamePlans

import Lifecycle.With

class GameplanModeTemplateVsRandom extends GameplanModeTemplate {
  
  override def isComplete: Boolean = super.isComplete && With.enemies.exists(e => e.isTerran || e.isProtoss || e.isZerg)
}
