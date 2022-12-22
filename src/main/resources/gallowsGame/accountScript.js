const gameServerPath = `http://localhost:8080/gallowsGame`;

let queryString = window.location.search;
let params = new URLSearchParams(queryString);

let username = encodeURIComponent(params.get("username"));
let passwordHash = encodeURIComponent(params.get("passwordHash"));

const categorySelect = document.getElementById("category-select");
const activeGamesListWrapper = document.getElementById("active-games-list-wrapper");

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

function createCategoriesSelect(categories) {
    categories = categories.split(",");
    for (const category of categories) {
        let option = document.createElement("option");
        option.setAttribute("value", category);
        option.textContent = category;
        categorySelect.append(option);
    }
}

async function createActiveGamesList(activeGames) {
    activeGames = activeGames.substring(1, activeGames.length - 1).split(", ");
    if (activeGames.length > 0 && activeGames[0] !== "") {
        const activeGamesList = document.createElement("ul");
        activeGamesList.classList.add("active-games-list");
        for (const gameId of activeGames) {
            const activeGameItem = document.createElement("li");
            activeGameItem.classList.add("active-game-item");

            const a = document.createElement("a");
            const gameURL = `${gameServerPath}/game?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`
            a.setAttribute("href", gameURL);

            let gameState = await fetch(`${gameServerPath}/getGameState?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`);
            gameState = parseGameState(await gameState.text());

            let wordLength = gameState["wordLength"];
            let guessedChars = gameState["guessedChars"];
            let wordPreview = "_".repeat(wordLength).split("");
            for (const [index, character] of Object.entries(guessedChars)) {
                wordPreview[index] = character;
            }

            a.textContent = wordPreview.join("");
            activeGameItem.append(a);
            activeGamesList.append(activeGameItem);
        }
        activeGamesListWrapper.append(activeGamesList);
    }
}

(async () => {
    let categories = await fetch(`${gameServerPath}/getCategories`);
    categories = await categories.text();
    createCategoriesSelect(categories);

    let activeGames = await fetch(`${gameServerPath}/getActiveGames?username=${username}&passwordHash=${passwordHash}`);
    activeGames = await activeGames.text();
    await createActiveGamesList(activeGames);

    let userInfo = await fetch(`${gameServerPath}/getUserInfo?username=${username}&passwordHash=${passwordHash}`);
    userInfo = await userInfo.text();
    let startIndex = userInfo.indexOf("[");
    let endIndex = userInfo.indexOf("]");
    userInfo = userInfo.substring(startIndex + 1, endIndex);
    userInfo = userInfo.split(", ");
    const totalGamesNumber = document.getElementById("total-games-number");
    const winsNumber = document.getElementById("wins-number");
    const losesNumber = document.getElementById("loses-number");
    const gamesBar = document.getElementById("games-bar");
    const winsBar = document.getElementById("wins-bar");

    let totalGames = Number(userInfo[2].substring(userInfo[2].indexOf("=") + 1));
    totalGamesNumber.textContent = "Всего игр: " + totalGames;

    let totalWins = Number(userInfo[3].substring(userInfo[3].indexOf("=") + 1));
    winsNumber.textContent = "Побед: " + totalWins;
    if (totalGames > 0) {
        let percents = totalWins / totalGames * 100;
        winsNumber.textContent += ", " + percents + "%";
        winsBar.style.width = `${percents}%`;
    }

    let totalLoses = totalGames - totalWins;
    losesNumber.textContent = "Поражений: " + totalLoses;


})();

async function startGame(category) {
    let encodedCategory = encodeURIComponent(category);
    let gameId = await fetch(`http://localhost:8080/gallowsGame/startGame?username=${username}&passwordHash=${passwordHash}&category=${encodedCategory}`);
    gameId = await gameId.text();

    let gameURL = `http://localhost:8080/gallowsGame/game?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`;
    console.log(gameURL);
    window.location.replace(gameURL);
}

const startGameButton = document.getElementById("start-game-button");
startGameButton.addEventListener("click", async (e) => {
    let category = categorySelect.value;
    console.log(category);
    await startGame(category);
})