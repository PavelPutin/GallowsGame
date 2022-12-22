const gameServerPath = `http://localhost:8080/gallowsGame`;

const queryString = window.location.search;
const params = new URLSearchParams(queryString);

const username = encodeURIComponent(params.get("username"));
const passwordHash = encodeURIComponent(params.get("passwordHash"));
const gameId = params.get("gameId");

const word = document.getElementById("word");
const keyboard = document.getElementById("keyboard");
const triesCounter = document.getElementById("tries-counter");
const gameStatusLabel = document.getElementById("game-status");

function parseGameState(rawGameState) {
    let regexp = /(?<=(\[|\{).+?([^\]\}])), (?=.+?(\]|\}))/gm
    let gameState = rawGameState.substring(rawGameState.indexOf("[") + 1, rawGameState.length - 1);
    gameState = gameState.replace(regexp, ",")
    gameState = gameState.split(", ");

    let obj = {};
    for (let i = 0; i < gameState.length; i++) {
        let index = gameState[i].indexOf("=");
        let key = gameState[i].substring(0, index);
        let value = gameState[i].substring(index + 1);

        if (key === "wordLength" || key ==="tries") {
            value = Number(value);
        } else if (key === "usedCharacters" || key === "guessedChars") {
            value = value.substring(1, value.length - 1);
            value = value.split(",");
            if (value[0] === "") {
                value = key === "usedCharacters" ? [] : {};
            } else if (key === "guessedChars") {
                let temp = {};
                for (let v of value) {
                    v = v.split("=");
                    temp[Number(v[0])] = v[1];
                }
                value = temp
            }
        }

        obj[key] = value;
    }

    return obj;
}
async function inputCharacter(e) {
    console.log("manual keyboard");
    let character = e.key.toLowerCase();
    await makeStep(character);
}

(async () => {
    let gameState = await fetch(`${gameServerPath}/getGameState?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`);
    gameState = await gameState.text();
    gameState = parseGameState(gameState);

    let length = gameState["wordLength"];
    let guessedChars = gameState["guessedChars"];
    createWord(length, guessedChars);

    let usedCharacters = gameState["usedCharacters"];
    createKeyboard(usedCharacters);

    let tries = gameState["tries"];
    updateTriesCounter(tries)

    let winStatus = gameState["winStatus"];
    updateGameStatusLabel(winStatus)

    document.addEventListener("keydown", inputCharacter);
})();

function createWord(length, guessedChars) {
    for (let i = 0; i < length; i++) {
        let wordElement = document.createElement("div");
        wordElement.classList.add("word-element");
        if (guessedChars[i] !== undefined) {
            wordElement.classList.add("word-element-guessed");
            wordElement.textContent = guessedChars[i];
        }
        word.appendChild(wordElement);
    }
}

function createKeyboard(usedCharacters) {
    let keyboardButtonCodes = [
        [1081, 1094, 1091, 1082, 1077, 1085, 1075, 1096, 1097, 1079, 1093, 1098],
        [1092, 1099, 1074, 1072, 1087, 1088, 1086, 1083, 1076, 1078, 1101],
        [1103, 1095, 1089, 1084, 1080, 1090, 1100, 1073, 1102]
    ];
    let specialCode = 1105;

    for (const line of keyboardButtonCodes) {
        let keyboardLine = document.createElement("div");
        keyboardLine.classList.add("keyboard-line");
        for (const code of line) {
            let keyboardElement = createKeyboardElement(code, usedCharacters);
            keyboardLine.appendChild(keyboardElement);
        }
        keyboard.appendChild(keyboardLine);
    }
    let specialElement = createSpecialKeyboardElement(specialCode, usedCharacters);
    keyboard.appendChild(specialElement);
}

function createKeyboardElement(code, usedCharacters) {
    let character = String.fromCharCode(code);

    let keyboardElement = document.createElement("button");
    keyboardElement.setAttribute("type", "button");
    keyboardElement.id = "key" + String(code);

    keyboardElement.classList.add("keyboard-element");
    if (usedCharacters.indexOf(character) !== -1) {
        keyboardElement.classList.add("keyboard-element-pressed");
        keyboardElement.setAttribute("disabled", "");
    }

    keyboardElement.textContent = character;
    keyboardElement.addEventListener("click", e => makeStep(character));
    return keyboardElement;
}

function createSpecialKeyboardElement(code, usedCharacters) {
    let keyboardLine = document.createElement("div");
    keyboardLine.classList.add("keyboard-line-special");

    let keyboardElement = createKeyboardElement(code, usedCharacters);
    keyboardElement.classList.add("keyboard-element");
    keyboardLine.appendChild(keyboardElement);
    return keyboardLine;
}

function isRussianLetter(code) {
    return 1072 <= code && code <= 1103 || code === 1105;
}

async function fetchGameState(character) {
    character = encodeURIComponent(character);
    return await fetch(`${gameServerPath}/makeChoice?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}&character=${character}`);
}

function openWord(targetWord) {
    for (let i = 0; i < targetWord.length; i++) {
        word.children[i].textContent = targetWord.charAt(i);
    }
}

function openGuessedElements(guessedChars) {
    for (const [index, character] of Object.entries(guessedChars)) {
        word.children[index].classList.add("word-element-guessed");
        word.children[index].textContent = character;
    }
}

function markPressedKeyboardElements(usedCharacters, character, code) {
    if (usedCharacters.indexOf(character) !== -1) {
        let id = "key" + String(code);
        let element = document.getElementById(id);
        disableKeyboardElement(element);
    }
}

function disableKeyboardElement(keyboardElement) {
    keyboardElement.classList.add("keyboard-element-pressed");
    keyboardElement.setAttribute("disabled", "");
}

function updateTriesCounter(tries) {
    triesCounter.textContent = tries;
}

function updateGameStatusLabel(winStatus) {
    if (winStatus === "IN_PROCESS") {return};
    gameStatusLabel.textContent = winStatus === "WIN" ? "Вы победили" : "Вы проиграли";
}

function disableKeyboard() {
    for (const line of keyboard.childNodes) {
        for (const key of line.childNodes) {
            console.log(key);
            disableKeyboardElement(key);
        }
    }
}

async function makeStep(character) {
    let code = character.charCodeAt(0);
    if (!isRussianLetter(code)) {
        return;
    }

    let gameState = await fetchGameState(character);
    gameState = parseGameState(await gameState.text());

    let guessedChars = gameState["guessedChars"];
    openGuessedElements(guessedChars);

    let usedCharacters = gameState["usedCharacters"];
    markPressedKeyboardElements(usedCharacters, character, code);

    let tries = gameState["tries"];
    updateTriesCounter(tries)

    let winStatus = gameState["winStatus"];
    if (winStatus !== "IN_PROCESS") {
        document.removeEventListener("keydown", inputCharacter, false);
        updateGameStatusLabel(winStatus);
        let targetWord = await fetch(`${gameServerPath}/endGame?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`);
        disableKeyboard();
        if (winStatus === "LOSE") {
            targetWord = await targetWord.text();
            openWord(targetWord);
        }
    }
}
