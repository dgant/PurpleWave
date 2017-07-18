package Micro.Decisions

trait MicroDecision {
  
  def value: Double
  def frames: Double
  
  def execute()
}
