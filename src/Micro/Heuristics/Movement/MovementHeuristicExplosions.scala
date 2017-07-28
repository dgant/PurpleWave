package Micro.Heuristics.Movement

import Mathematics.Points.Pixel
import Micro.Agency.{Agent, Explosion}

object MovementHeuristicExplosions extends MovementHeuristic {
  
  override def evaluate(state: Agent, candidate: Pixel): Double = {
  
    state.explosions.map(evaluateExplosion(state, candidate, _)).sum
  }
  
  private def evaluateExplosion(
                                 state     : Agent,
                                 candidate : Pixel,
                                 explosion : Explosion): Double = {
  
    explosion.damage *
      Math.max(
      0.0,
        explosion.radius
        + state.unit.unitClass.radialHypotenuse
        + 32.0
        - candidate.pixelDistanceFast(explosion.center))
  }
}
