import scala.collection.mutable
import scala.collection.mutable._
import scala.io.StdIn._
/**
  * Created by anike_000 on 2/15/2017.
  */
object School extends App{

  var courses: HashSet[String] = HashSet[String]("Math","Science","History","Geography")
  var students: HashSet[String] = new HashSet[String]()
  var enrollment: HashMap[String, ArrayBuffer[String]] = new HashMap[String, ArrayBuffer[String]]()

  var option = 1
  while(option != 0){
    println("Enter an option from below:")

    println("1. Information")
    println("2. Enroll Class")
    println("3. Cancel Enrollment")
    println("4. Cancel Class")
    println("5. Add New Student")
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
        }

        case 2 => {
          println("Okay!! lets enroll class")
          println("Please enter below information")
          println("Enter student name to be enrolled")
          var studentName = readLine()

          // checking if the student is valid
          if(students.contains(studentName)) {
            println("Enter class name to be enrolled")
            var className = readLine()
            if(courses.contains(className)){
              if(enrollment.contains(className) == true){
                var al: Option[ArrayBuffer[String]] = enrollment.get(className)
                al + studentName
              }
              else{
                var al = new ArrayBuffer[String]()
                al + studentName
                enrollment.put(className,al)
              }
            }
            else{
              println("Invalid student")
              println("Please start again")
            }
          }
          else{
            println("Invalid student")
            println("Please start again")
          }

        }
    }
  }

}
