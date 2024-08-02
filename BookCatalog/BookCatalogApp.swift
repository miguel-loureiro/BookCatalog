//
//  BookCatalogApp.swift
//  BookCatalog
//
//  Created by Miguel Loureiro on 02/08/2024.
//

import SwiftUI

@main
struct BookCatalogApp: App {
    let persistenceController = PersistenceController.shared

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.managedObjectContext, persistenceController.container.viewContext)
        }
    }
}
