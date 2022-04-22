package Information.Geography.Types

import scala.collection.mutable
import scala.util.Random

class LabelGenerator(labels: Iterable[String]) {
  private var repeats: Int = 1
  private val names   = Random.shuffle(labels)
  private val nameQ   = new mutable.Queue[String] ++ names
  def next(): String = {
    if (nameQ.isEmpty) {
      repeats += 1
      nameQ ++= names.map(name => f"$name $repeats")
    }
    nameQ.dequeue()
  }
}
