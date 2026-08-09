package com.practice.app.data

enum class EmergencyRelationship(val label: String) {
    Friend("Friend"),
    Relative("Relative"),
    Colleague("Colleague"),
    Other("Other"),
}

data class Client(
    val id: String,
    val firstName: String,
    val surname: String,
    val dateOfBirth: String,
    val mobile: String,
    val emergencyContactName: String = "",
    val emergencyContactNumber: String = "",
    val relationship: EmergencyRelationship? = null,
    val isActive: Boolean,
    /** When the client was added — newest appear first on the Active list. */
    val createdAtEpochMs: Long = 0L,
) {
    val fullName: String
        get() = "$firstName $surname"
}

/** Formats typed input as DD/MM/YYYY, inserting slashes automatically. */
fun formatDateOfBirthInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { index, digit ->
            if (index == 2 || index == 4) append('/')
            append(digit)
        }
    }
}

/**
 * Same formatting as [formatDateOfBirthInput], but keeps the cursor after the same
 * digit count so auto-inserted slashes don't send the caret backwards.
 */
fun formatDateOfBirthSelection(
    text: String,
    selectionEnd: Int,
): Pair<String, Int> {
    val safeSelection = selectionEnd.coerceIn(0, text.length)
    val digitsBeforeCursor = text.take(safeSelection).count { it.isDigit() }
    val formatted = formatDateOfBirthInput(text)

    if (digitsBeforeCursor <= 0) {
        return formatted to 0
    }

    var seenDigits = 0
    var cursor = formatted.length
    for (index in formatted.indices) {
        if (formatted[index].isDigit()) {
            seenDigits++
            if (seenDigits == digitsBeforeCursor) {
                cursor = index + 1
                break
            }
        }
    }

    // Move past an auto-inserted slash so the next digit lands in the right place.
    if (cursor < formatted.length && formatted[cursor] == '/') {
        cursor++
    }

    return formatted to cursor.coerceIn(0, formatted.length)
}

/**
 * Bump when adding new seed clients. Existing installs merge in missing IDs only —
 * never replace or delete user-created clients/sessions.
 */
const val ClientSeedVersion = 3

val PlaceholderClients: List<Client> = listOf(
    // Original sample clients (kept so they can be restored after earlier wipes)
    Client(
        id = "celeste-warwick",
        firstName = "Celeste",
        surname = "Warwick",
        dateOfBirth = "21/04/1971",
        mobile = "0499882101",
        isActive = true,
    ),
    Client(
        id = "vincent-huang",
        firstName = "Vincent",
        surname = "Huang",
        dateOfBirth = "01/12/1991",
        mobile = "0405155320",
        isActive = true,
    ),
    Client(
        id = "andrew-james",
        firstName = "Andrew",
        surname = "James",
        dateOfBirth = "21/12/1987",
        mobile = "0490693182",
        isActive = false,
    ),
    Client(
        id = "alex-lee",
        firstName = "Alex",
        surname = "Lee",
        dateOfBirth = "15/09/2001",
        mobile = "0477852221",
        isActive = false,
    ),
    Client(
        id = "amelia-hart",
        firstName = "Amelia",
        surname = "Hart",
        dateOfBirth = "14/03/1988",
        mobile = "0412 883 014",
        emergencyContactName = "Daniel Hart",
        emergencyContactNumber = "0418 220 931",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "benjamin-okonkwo",
        firstName = "Benjamin",
        surname = "Okonkwo",
        dateOfBirth = "02/11/1992",
        mobile = "0423 551 208",
        emergencyContactName = "Chioma Okonkwo",
        emergencyContactNumber = "0431 774 650",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "cara-nguyen",
        firstName = "Cara",
        surname = "Nguyen",
        dateOfBirth = "27/07/1995",
        mobile = "0401 662 387",
        emergencyContactName = "Minh Nguyen",
        emergencyContactNumber = "0490 113 244",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "dylan-fraser",
        firstName = "Dylan",
        surname = "Fraser",
        dateOfBirth = "09/01/1984",
        mobile = "0433 908 172",
        emergencyContactName = "Sophie Lane",
        emergencyContactNumber = "0417 556 039",
        relationship = EmergencyRelationship.Friend,
        isActive = true,
    ),
    Client(
        id = "elena-vasquez",
        firstName = "Elena",
        surname = "Vasquez",
        dateOfBirth = "19/05/1979",
        mobile = "0455 221 764",
        emergencyContactName = "Marco Vasquez",
        emergencyContactNumber = "0429 880 413",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "finn-mcallister",
        firstName = "Finn",
        surname = "McAllister",
        dateOfBirth = "30/09/1998",
        mobile = "0467 334 901",
        emergencyContactName = "Hannah Price",
        emergencyContactNumber = "0408 219 675",
        relationship = EmergencyRelationship.Friend,
        isActive = true,
    ),
    Client(
        id = "grace-patel",
        firstName = "Grace",
        surname = "Patel",
        dateOfBirth = "11/12/1990",
        mobile = "0419 772 508",
        emergencyContactName = "Ravi Patel",
        emergencyContactNumber = "0432 145 890",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "harry-bennett",
        firstName = "Harry",
        surname = "Bennett",
        dateOfBirth = "06/04/1986",
        mobile = "0471 603 229",
        emergencyContactName = "Olivia Bennett",
        emergencyContactNumber = "0415 998 307",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "isla-morrison",
        firstName = "Isla",
        surname = "Morrison",
        dateOfBirth = "22/08/1993",
        mobile = "0426 814 550",
        emergencyContactName = "Jack Morrison",
        emergencyContactNumber = "0491 267 138",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "james-okafor",
        firstName = "James",
        surname = "Okafor",
        dateOfBirth = "15/02/1975",
        mobile = "0403 457 612",
        emergencyContactName = "Ngozi Adeyemi",
        emergencyContactNumber = "0444 120 986",
        relationship = EmergencyRelationship.Friend,
        isActive = true,
    ),
    Client(
        id = "katie-liu",
        firstName = "Katie",
        surname = "Liu",
        dateOfBirth = "03/06/2000",
        mobile = "0482 931 740",
        emergencyContactName = "Wei Liu",
        emergencyContactNumber = "0411 508 263",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "liam-donovan",
        firstName = "Liam",
        surname = "Donovan",
        dateOfBirth = "28/10/1981",
        mobile = "0438 265 914",
        emergencyContactName = "Erin Donovan",
        emergencyContactNumber = "0450 773 182",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "maya-singh",
        firstName = "Maya",
        surname = "Singh",
        dateOfBirth = "17/03/1997",
        mobile = "0416 409 835",
        emergencyContactName = "Priya Kapoor",
        emergencyContactNumber = "0422 651 074",
        relationship = EmergencyRelationship.Friend,
        isActive = true,
    ),
    Client(
        id = "noah-whitfield",
        firstName = "Noah",
        surname = "Whitfield",
        dateOfBirth = "08/07/1989",
        mobile = "0460 188 527",
        emergencyContactName = "Claire Whitfield",
        emergencyContactNumber = "0435 902 416",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "olivia-chen",
        firstName = "Olivia",
        surname = "Chen",
        dateOfBirth = "25/01/1994",
        mobile = "0498 734 061",
        emergencyContactName = "David Chen",
        emergencyContactNumber = "0407 319 852",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "patrick-omalley",
        firstName = "Patrick",
        surname = "O'Malley",
        dateOfBirth = "12/09/1972",
        mobile = "0428 546 193",
        emergencyContactName = "Siobhan O'Malley",
        emergencyContactNumber = "0413 870 265",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "quinn-adelaide",
        firstName = "Quinn",
        surname = "Adelaide",
        dateOfBirth = "04/05/1991",
        mobile = "0459 012 748",
        emergencyContactName = "Sam Rivera",
        emergencyContactNumber = "0477 431 609",
        relationship = EmergencyRelationship.Colleague,
        isActive = true,
    ),
    Client(
        id = "ruby-tanaka",
        firstName = "Ruby",
        surname = "Tanaka",
        dateOfBirth = "21/11/1987",
        mobile = "0409 685 324",
        emergencyContactName = "Kenji Tanaka",
        emergencyContactNumber = "0439 158 070",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "samuel-brooks",
        firstName = "Samuel",
        surname = "Brooks",
        dateOfBirth = "16/08/1996",
        mobile = "0441 273 896",
        emergencyContactName = "Laura Brooks",
        emergencyContactNumber = "0425 640 113",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "tara-mikhail",
        firstName = "Tara",
        surname = "Mikhail",
        dateOfBirth = "29/04/1983",
        mobile = "0485 917 402",
        emergencyContactName = "Yasmin Farouk",
        emergencyContactNumber = "0410 364 758",
        relationship = EmergencyRelationship.Friend,
        isActive = true,
    ),
    Client(
        id = "uma-desai",
        firstName = "Uma",
        surname = "Desai",
        dateOfBirth = "07/12/1978",
        mobile = "0430 528 641",
        emergencyContactName = "Anika Desai",
        emergencyContactNumber = "0466 189 305",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "victor-lange",
        firstName = "Victor",
        surname = "Lange",
        dateOfBirth = "13/06/1999",
        mobile = "0474 850 219",
        emergencyContactName = "Helena Lange",
        emergencyContactNumber = "0402 793 564",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "willow-ashford",
        firstName = "Willow",
        surname = "Ashford",
        dateOfBirth = "01/10/1985",
        mobile = "0414 136 987",
        emergencyContactName = "Tom Ashford",
        emergencyContactNumber = "0452 408 731",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "xavier-monteiro",
        firstName = "Xavier",
        surname = "Monteiro",
        dateOfBirth = "24/02/1992",
        mobile = "0421 769 053",
        emergencyContactName = "Isabel Monteiro",
        emergencyContactNumber = "0488 215 640",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "yasmin-clarke",
        firstName = "Yasmin",
        surname = "Clarke",
        dateOfBirth = "18/07/1980",
        mobile = "0463 094 572",
        emergencyContactName = "Matthew Clarke",
        emergencyContactNumber = "0436 821 907",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "zachary-reid",
        firstName = "Zachary",
        surname = "Reid",
        dateOfBirth = "05/03/2001",
        mobile = "0493 657 180",
        emergencyContactName = "Natalie Reid",
        emergencyContactNumber = "0418 042 396",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "bethany-koura",
        firstName = "Bethany",
        surname = "Koura",
        dateOfBirth = "10/09/1976",
        mobile = "0405 318 724",
        emergencyContactName = "Joseph Koura",
        emergencyContactNumber = "0447 960 251",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "callum-wright",
        firstName = "Callum",
        surname = "Wright",
        dateOfBirth = "26/05/1994",
        mobile = "0479 142 608",
        emergencyContactName = "Jess Wright",
        emergencyContactNumber = "0427 583 914",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "diana-rossi",
        firstName = "Diana",
        surname = "Rossi",
        dateOfBirth = "20/01/1982",
        mobile = "0434 705 839",
        emergencyContactName = "Paolo Rossi",
        emergencyContactNumber = "0456 291 047",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
    Client(
        id = "ethan-nguyen-tran",
        firstName = "Ethan",
        surname = "Tran",
        dateOfBirth = "31/08/1997",
        mobile = "0417 864 320",
        emergencyContactName = "Linh Tran",
        emergencyContactNumber = "0492 510 678",
        relationship = EmergencyRelationship.Relative,
        isActive = true,
    ),
)
