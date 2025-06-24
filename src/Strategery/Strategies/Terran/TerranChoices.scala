package Strategery.Strategies.Terran

import Strategery.Strategies._

object DefaultTerran extends Strategy

object TerranChoices {
  
  val tvr: Vector[Strategy] = Vector(
    DefaultTerran
  )
  
  val tvtOpeners: Vector[Strategy] = Vector(
    DefaultTerran
  )
  
  val tvpOpeners: Vector[Strategy] = Vector(
    DefaultTerran
  )
  
  val tvzOpeners: Vector[Strategy] = Vector(
    DefaultTerran
  )

  val all: Vector[Strategy] = (tvr ++ tvtOpeners ++ tvpOpeners ++ tvzOpeners).distinct
}