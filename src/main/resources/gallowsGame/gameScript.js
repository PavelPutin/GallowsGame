const gameServerPath = `http://localhost:8080/gallowsGame`;

const queryString = window.location.search;
const params = new URLSearchParams(queryString);

const username = encodeURIComponent(params.get("username"));
const passwordHash = encodeURIComponent(params.get("passwordHash"));
const gameId = params.get("gameId");

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
        } else if (key === "usedCharacters" || key === "openChars") {
            value = value.substring(1, value.length - 1);
            value = value.split(",");
            if (value[0] === "") {
                value = key === "usedCharacters" ? [] : {};
            } else if (key === "openChars") {
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


(async () => {
    let gameState = await fetch(`${gameServerPath}/getGameState?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`);
    gameState = await gameState.text();
    gameState = parseGameState(gameState);

    let length = gameState["wordLength"];
    let openChars = gameState["openChars"];
    const word = document.getElementById("word");
    for (let i = 0; i < length; i++) {
        let wordElement = document.createElement("div");
        wordElement.classList.add("word-element");
        if (openChars[i] !== undefined) {
            wordElement.classList.add("word-element-open");
            wordElement.textContent = openChars[i];
        }
        word.appendChild(wordElement);
    }

    const keyboard = document.getElementById("keyboard");
    let keyboardButtonCodes = [
        [1081, 1094, 1091, 1082, 1077, 1085, 1075, 1096, 1097, 1079, 1093, 1098],
        [1092, 1099, 1074, 1072, 1087, 1088, 1086, 1083, 1076, 1078, 1101],
        [1103, 1095, 1089, 1084, 1080, 1090, 1100, 1073, 1102]
    ];
    let specialCode = 1105;

    let usedCharacters = gameState["usedCharacters"];
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
})();


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
    let keyboardElement = createKeyboardElement(code, usedCharacters);
    keyboardElement.classList.add("keyboard-element-special");
    return keyboardElement;
}


function isRussianLetter(code) {
    return 1072 <= code && code <= 1103 || code === 1105;
}

async function fetchGameState(character) {
    character = encodeURIComponent(character);
    return await fetch(`${gameServerPath}/makeChoice?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}&character=${character}`);
}

function openWordElements(openChars) {
    const word = document.getElementById("word");
    for (const [index, character] of Object.entries(openChars)) {
        word.children[index].textContent = character;
    }
}

function markGuessedElements(openChars) {
    const word = document.getElementById("word");
    for (const [index, character] of Object.entries(openChars)) {
        word.children[index].classList.add("word-element-guessed");
    }
}

function markPressedKeyboardElements(usedCharacters, character, code) {
    if (usedCharacters.indexOf(character) !== -1) {
        let id = "key" + String(code);
        let element = document.getElementById(id);
        element.classList.add("keyboard-element-pressed");
        element.setAttribute("disabled", "");
    }
}

async function makeStep(character) {
    let code = character.charCodeAt(0);

    if (!isRussianLetter(code)) {
        return;
    }
    let gameState = await fetchGameState(character);
    gameState = parseGameState(await gameState.text());

    let openChars = gameState["openChars"];
    openWordElements(openChars);

    let usedCharacters = gameState["usedCharacters"];
    markPressedKeyboardElements(usedCharacters, character, code);

    let winStatus = gameState["winStatus"];
    if (winStatus === "LOSE") {
        await fetch(`${gameServerPath}/endGame?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`);
    } else {
        markGuessedElements(openChars);
    }
}

window.addEventListener("keydown", e => {
    let character = e.key.toLowerCase();
    makeStep(character);
});