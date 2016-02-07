package example

class Inhabitants extends Iterable[Username] {
  private var currentUsers: Seq[Username] = Nil

  def add(user: Username): Boolean = {
    if (currentUsers.contains(user)) false
    else {
      currentUsers = currentUsers :+ user
      true
    }
  }

  def remove(user: Username): Boolean = {
    if (currentUsers.contains(user)) {
      currentUsers = currentUsers.filterNot(_ == user)
      true
    } else false
  }

  override def iterator: Iterator[Username] = currentUsers.iterator
}
