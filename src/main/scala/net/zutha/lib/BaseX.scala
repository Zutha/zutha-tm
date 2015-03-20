package net.zutha.lib

case class BaseX(charset: String){
  val baze = charset.length

  def encode(value: Long): String = {
    def process(q: Long, place: Int, builder: StringBuilder): StringBuilder = {
      if (q > 0) process(q / baze, place + 1, builder += symbol( (q % baze).toInt))
      else builder
    }
    if (value == 0) symbol(0).toString
    else process(value, 0, new StringBuilder(32)).reverse.toString
  }

  def decode(str: String): Long = {
    def process(acc: Long, place: Int, str: String, index: Int): Long = {
      if (index >= 0) process(acc + value(str.charAt(index)) * place, place * baze, str, index-1)
      else acc
    }
    process(0, 1, str, str.length - 1)
  }

  private def value(c: Char): Int = charset.indexOf(c)

  private def symbol(i: Int): Char = charset(i)

}
