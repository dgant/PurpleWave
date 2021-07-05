package Planning.Plans.GamePlans

trait Modal {
  def completed: Boolean
  def update(): Unit
}
