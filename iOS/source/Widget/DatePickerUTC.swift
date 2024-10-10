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
   @Binding private var minimumDate: Date?
   @Binding private var maximumDate: Date?
   
   init(selection: Binding<Date>, earliestDate: Binding<Date?> = .constant(nil), latestDate: Binding<Date?> = .constant(nil)) {
      self._selection = selection
      self._minimumDate = earliestDate
      self._maximumDate = latestDate
   }
   
   func makeUIView(context: Context) -> UIDatePicker {
      let datePicker = UIDatePicker()
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
      return uiView.bounds.size
   }
   
   func updateUIView(_ uiView: UIDatePicker, context: Context) {
      uiView.date = selection
      uiView.minimumDate = minimumDate
      uiView.maximumDate = maximumDate
      if context.coordinator.width == UIView.noIntrinsicMetric {
         uiView.sizeToFit()
         context.coordinator.width = uiView.bounds.size.width + 20
         uiView.bounds.size.width = context.coordinator.width
      }
   }
   
   func makeCoordinator() -> Coordinator {
      Coordinator(selection: $selection)
   }
   
   class Coordinator: NSObject {
      private let selection: Binding<Date>
      var width: CGFloat = UIView.noIntrinsicMetric
      
      init(selection: Binding<Date>, in range: ClosedRange<Date>? = nil) {
         self.selection = selection
      }
      
      @objc func changed(_ sender: UIDatePicker) {
         self.selection.wrappedValue = sender.date
      }
   }
}
