import scala.collection.mutable.{ArrayBuffer, HashMap, HashSet}
import scala.io.StdIn._

/**
  * Created by anike_000 on 2/15/2017.
  */
object SchoolEvent extends App {
  var courses: HashSet[String] = HashSet[String]("Math", "Science", "History", "Geography")
  var students: HashSet[String] = new HashSet[String]()
  var enrollment: HashMap[String, ArrayBuffer[String]] = new HashMap[String, ArrayBuffer[String]]()

  abstract class event

  case class addClass(className: String) extends event

  case class addStudent(studentName: String) extends event

  case class enrollClass(studentName: String, className: String) extends event

  case class cancelEnrollment(studentName: String, className: String) extends event

  case class cancelClass(className: String) extends event

  object eventHandler{

    def handler(e: event): Unit ={
      e match {
          case addClass(className: String) => courses.add(className)

          case addStudent(studentName: String) => students.add(studentName)

          case cancelClass(className: String) => {
            courses.remove(className)
            enrollment.remove(className)
          }

          case enrollClass(studentName: String, className: String) => {
            // className should already be there
            if(courses.contains(className)){
              println(className+" exists")
              if(students contains studentName){
                println(studentName+" exists")

                var aB = enrollment.getOrElse(className,new ArrayBuffer[String]())
                aB += studentName
                enrollment.put(className, aB)

              }
              else{
                println("Invalid Student")
                println("Please enter correct student name or add student(option 5) before enrolling classes")
              }
            }
            else{
              println("Invalid class name")
              println("Please enter correct class name or add class (option 6) before enrolling")
            }
          }

          case cancelEnrollment(studentName: String, className: String) => {
            if(enrollment contains className) {
              var aB = enrollment.getOrElse(className, new ArrayBuffer[String]())
              aB -= studentName
              //println(aB)
              enrollment put(className, aB)
            }
          }

      }
    }

  }

  var option = 1
  while (option != 0) {
    println("Enter an option from below:")

    println("1. Information")
    println("2. Enroll Class")
    println("3. Cancel Enrollment")
    println("4. Cancel Class")
    println("5. Add New Student")
    println("6. Add New Class")
    println("0. Exit")

    option = readInt()

    option match {
      case 0 => println("Exiting!!")

      case 1 => {
        println("Available courses for enrollment")
        println(courses)
        println("-------------")
        println("Enrolled students")
        println(students)
        println("Current Enrollments")
        println(enrollment)
      }

      case 2 => {
        println("Enter student name to be enrolled")
        val studentName = readLine()
        println("Enter class name to be enrolled")
        val className = readLine()
        eventHandler.handler(new enrollClass(studentName, className))
      }

      case 3 => {
        println("Enter Student Name")
        val studentName = readLine()
        println("Enter Class Name")
        val className = readLine()
        eventHandler.handler(new cancelEnrollment(studentName,className))
      }

      case 4 => {
        println("Enter class name to be cancelled")
        val className = readLine()
        eventHandler.handler(new cancelClass(className))
      }

      case 5 => {
        println("Enter student name to be added")
        val studentName = readLine()
        eventHandler.handler(new addStudent(studentName))
      }

      case 6 => {
        println("Enter class name to be added")
        val className = readLine()
        eventHandler.handler(new addClass(className))
      }
    }

    }
}
