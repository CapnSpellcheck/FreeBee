//
//  HexagonShape.swift
//  Free Bee
//
//  Created by G on 9/13/24.
//

import SwiftUI

struct HexagonShape: Shape {
   func path(in rect: CGRect) -> Path {
      var path = Path()
      let center = CGPoint(x: rect.midX, y: rect.midY)
      let radius = min(rect.size.height, rect.size.width) / 1.732
      let vertices = vertices(center: center, radius: radius)
      path.move(to: vertices[0])
      vertices[1...5].forEach() { point in
         path.addLine(to: point)
      }
      path.closeSubpath()
      return path
   }
   
   func vertices(center: CGPoint, radius: CGFloat) -> [CGPoint] {
      var points: [CGPoint] = []
      for i in (0...5) {
         let angle = CGFloat.pi / 3 * CGFloat(i)
         let point = CGPoint(
            x: center.x + radius * cos(angle),
            y: center.y + radius * sin(angle)
         )
         points.append(point)
      }
      return points
   }
}
