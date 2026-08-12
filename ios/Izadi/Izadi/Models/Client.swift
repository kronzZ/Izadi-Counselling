import Foundation

enum EmergencyRelationship: String, Codable, CaseIterable, Identifiable {
    case friend = "Friend"
    case relative = "Relative"
    case colleague = "Colleague"
    case partnerDefacto = "PartnerDefacto"
    case other = "Other"

    var id: String { rawValue }

    var label: String {
        switch self {
        case .partnerDefacto: return "Partner/Defacto"
        default: return rawValue
        }
    }

    /// Accepts stored Firestore values (enum name or display label).
    static func fromStored(_ value: String?) -> EmergencyRelationship? {
        guard let value, !value.isEmpty else { return nil }
        if let match = EmergencyRelationship(rawValue: value) { return match }
        return allCases.first { $0.label == value || $0.rawValue == value }
    }

    /// Reads `relationship` + optional `relationshipCustom` from Firestore.
    static func parseStored(
        relationship: String?,
        relationshipCustom: String
    ) -> (EmergencyRelationship?, String) {
        if let relationship, let match = fromStored(relationship) {
            if match == .other {
                return (.other, relationshipCustom)
            }
            return (match, "")
        }

        // Legacy: free-text relationship stored in `relationship` alone.
        if let relationship, !relationship.isEmpty {
            return (.other, relationship)
        }

        return (nil, "")
    }
}

struct Client: Identifiable, Equatable, Hashable {
    var id: String
    var firstName: String
    var surname: String
    var dateOfBirth: String
    var mobile: String
    var emergencyContactName: String
    var emergencyContactNumber: String
    var relationship: EmergencyRelationship?
    /// Free text when `relationship` is `.other`.
    var relationshipCustom: String
    var isActive: Bool
    var createdAtEpochMs: Int64

    var fullName: String { "\(firstName) \(surname)" }

    var relationshipDisplayLabel: String? {
        guard let relationship else { return nil }
        if relationship == .other {
            let trimmed = relationshipCustom.trimmingCharacters(in: .whitespacesAndNewlines)
            return trimmed.isEmpty ? relationship.label : trimmed
        }
        return relationship.label
    }

    init(
        id: String = UUID().uuidString,
        firstName: String,
        surname: String,
        dateOfBirth: String = "",
        mobile: String = "",
        emergencyContactName: String = "",
        emergencyContactNumber: String = "",
        relationship: EmergencyRelationship? = nil,
        relationshipCustom: String = "",
        isActive: Bool = true,
        createdAtEpochMs: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.firstName = firstName
        self.surname = surname
        self.dateOfBirth = dateOfBirth
        self.mobile = mobile
        self.emergencyContactName = emergencyContactName
        self.emergencyContactNumber = emergencyContactNumber
        self.relationship = relationship
        self.relationshipCustom = relationshipCustom
        self.isActive = isActive
        self.createdAtEpochMs = createdAtEpochMs
    }
}

enum DateOfBirthFormatting {
    static func format(_ raw: String) -> String {
        let digits = raw.filter(\.isNumber).prefix(8)
        var result = ""
        for (index, digit) in digits.enumerated() {
            if index == 2 || index == 4 { result.append("/") }
            result.append(digit)
        }
        return result
    }
}
