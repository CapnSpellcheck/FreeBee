// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "FreeBeeData",
    platforms: [.iOS("15.6")],
    products: [
        .library(
            name: "FreeBeeData",
            type: .static,
            targets: ["FreeBeeData"])
    ],
    dependencies: [    ],
    targets: [
        .target(
            name: "FreeBeeData",
            dependencies: [            ],
            path: "FreeBeeData")
        
    ]

)