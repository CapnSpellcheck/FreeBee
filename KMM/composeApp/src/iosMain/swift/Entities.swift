import Foundation
import CoreData

@objc public class CDGame : NSManagedObject, Identifiable {
   public class func fetchRequest() -> NSFetchRequest<CDGame> {
        return NSFetchRequest<CDGame>(entityName: "Game")
    }
   
   @NSManaged public var allowedWords: Set<String>
   @NSManaged public var centerLetterCode: Int32
   @NSManaged public var date: Date?
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
