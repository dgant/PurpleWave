package Planning.Plans.GamePlans

import Lifecycle.With

class GameplanTemplateVsRandom extends GameplanTemplate {
  
  override def completed: Boolean = super.completed && With.enemies.exists(e => e.isTerran || e.isProtoss || e.isZerg)
}
