// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "KannaWrapper",
    platforms: [.iOS("15.6")],
    products: [
        .library(
            name: "KannaWrapper",
            type: .static,
            targets: ["KannaWrapper"])
    ],
    dependencies: [
        .package(url: "https://github.com/tid-kijyun/Kanna", exact: "5.3.0")
    ],
    targets: [
        .target(
            name: "KannaWrapper",
            dependencies: [
                .product(name: "Kanna", package: "Kanna")
            ],
            path: "KannaWrapper")
        
    ]

)