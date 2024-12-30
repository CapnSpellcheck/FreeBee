import Foundation
import CoreData

/**
 * I chose to make this compatible with the database of the pure iOS app, hence 'date' is a Date.
 * In the KMM code I chose to use LocalDate to represent the 'date' since the moment of time is
 * not important. The game is represented precisely by the (calendar) date. Because of this, you
 * see some hoops are gone through to convert the Date 'date' here to LocalDate. If I were making
 * the app fresh with no compatibility concern, I'd just store "epoch days" for date like I do on
 * Android. 
 */
@objc public class CDGame : NSManagedObject, Identifiable {
   public class func fetchRequest() -> NSFetchRequest<CDGame> {
        return NSFetchRequest<CDGame>(entityName: "Game")
    }
   
   @NSManaged public var allowedWords: Set<String>
   @NSManaged public var centerLetterCode: Int32
   @NSManaged public var date: Date?
   // dirtyTrigger needed because the score is updated via CDGameProgress, whereas the root screen uses
   // a fetch request of this class. The fetch request doesn't note changes in its relationship's
   // attributes (https://stackoverflow.com/questions/12370521/changing-a-managed-object-property-doesnt-trigger-nsfetchedresultscontroller-to).
   // It's transient in xcdatamodel
   @NSManaged public var dirtyTrigger: Int16
   @NSManaged public var geniusScore: Int16
   @NSManaged public var maximumScore: Int16
   @NSManaged public var otherLetters: String
   @NSManaged public var progress: CDGameProgress
   
   override public func awakeFromInsert() {
      progress = CDGameProgress(context: managedObjectContext!)
   }
}

@objc public class CDGameProgress : NSManagedObject, Identifiable {
   public class func fetchRequest() -> NSFetchRequest<CDGameProgress> {
       return NSFetchRequest<CDGameProgress>(entityName: "GameProgress")
   }

   @NSManaged public var currentWord: String
   @NSManaged public var score: Int16
   @NSManaged public var enteredWords: NSOrderedSet
   @NSManaged public var game: CDGame
   
   override public func awakeFromInsert() {
      currentWord = ""
   }
   
   @objc(addEnteredWordsObject:)
   @NSManaged public func addToEnteredWords(_ value: CDEnteredWord)
}

@objc public class CDEnteredWord : NSManagedObject, Identifiable {
    public class func fetchRequest() -> NSFetchRequest<CDEnteredWord> {
        return NSFetchRequest<CDEnteredWord>(entityName: "EnteredWord")
    }
    @objc public convenience init(context: NSManagedObjectContext, string: String) {
        self.init(context: context)
        value = string
    }
    @NSManaged public var value: String
}
