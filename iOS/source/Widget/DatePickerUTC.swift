//
//  DatePicker.swift
//  Free Bee
//
//  Created by G on 10/1/24.
//

import Foundation
import SwiftUI

struct DatePickerUTC: UIViewRepresentable {
   @Binding private var selection: Date
   private let range: ClosedRange<Date>?
   private let datePicker = UIDatePicker()

   private var minimumDate: Date? {
      range?.lowerBound
   }
   
   private var maximumDate: Date? {
      range?.upperBound
   }
   
   init(selection: Binding<Date>, in range: ClosedRange<Date>?) {
      self._selection = selection
      self.range = range
   }
   
   func makeUIView(context: Context) -> UIDatePicker {
      datePicker.datePickerMode = .date
      datePicker.preferredDatePickerStyle = .compact
      datePicker.minimumDate = minimumDate
      datePicker.maximumDate = maximumDate
      datePicker.timeZone = TimeZone(identifier: "UTC")
      datePicker.addTarget(context.coordinator, action: #selector(Coordinator.changed(_:)), for: .valueChanged)
      return datePicker
   }
   
   @available(iOS 16.0, *)
   func sizeThatFits(
       _ proposal: ProposedViewSize,
       uiView: Self.UIViewType,
       context: Self.Context
   ) -> CGSize? {
      datePicker.sizeThatFits(.zero)
   }
   
   func updateUIView(_ uiView: UIDatePicker, context: Context) {
      datePicker.date = selection
      
   }
   
   func makeCoordinator() -> Coordinator {
      Coordinator(selection: $selection, in: range)
   }
   
   class Coordinator: NSObject {
      private let selection: Binding<Date>
      private let range: ClosedRange<Date>?
      
      init(selection: Binding<Date>, in range: ClosedRange<Date>? = nil) {
         self.selection = selection
         self.range = range
      }
      
      @objc func changed(_ sender: UIDatePicker) {
         self.selection.wrappedValue = sender.date
      }
   }
}
