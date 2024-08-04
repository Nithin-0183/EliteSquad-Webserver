function loadServers() {
    fetch('/admin/server-status')
        .then(response => response.json())
        .then(data => {
            const tableBody = document.getElementById('server-list');
            tableBody.innerHTML = ''; // Clear the table before appending

            data.forEach(server => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${server.domain}</td>
                    <td>${server.ipAddress}</td>
                    <td><button onclick="removeServer('${server.domain}')">Remove</button></td>
                `;
                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching server status:', error));
}

function removeServer(domain) {
    fetch('/admin/remove-server', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ domain })
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message);
        loadServers();
    })
    .catch(error => console.error('Error:', error));
}

function initAddRemoveListeners() {
    const popup = document.getElementById('add-server-popup');
    const closeButton = document.querySelector('.close-button');

    document.getElementById('add-server-button').addEventListener('click', () => {
        popup.style.display = 'flex';
    });

    closeButton.addEventListener('click', () => {
        popup.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === popup) {
            popup.style.display = 'none';
        }
    });
}
