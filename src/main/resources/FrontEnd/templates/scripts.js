document.addEventListener('DOMContentLoaded', () => {
    // Handle login form submission
    if (document.getElementById('login-form')) {
        document.getElementById('login-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            const response = await fetch('/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            const result = await response.json();
            if (response.status === 200) {
                window.location.href = '/admin.html';
            } else {
                alert(result.message);
            }
        });
    }

    // Load server status
    if (document.getElementById('server-status-body')) {
        fetch('/admin/server-status')
            .then(response => response.json())
            .then(data => {
                const tableBody = document.getElementById('server-status-body');
                data.forEach(server => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${server.domain}</td>
                        <td>${server.ip}:${server.port}</td>
                        <td class="${server.status === 'Running' ? 'status-running' : 'status-off'}">${server.status}</td>
                    `;
                    tableBody.appendChild(row);
                });
            });
    }

    // Load existing servers and handle add server form submission
    if (document.getElementById('add-server-form')) {
        const loadServers = () => {
            fetch('/admin/server-status')
                .then(response => response.json())
                .then(data => {
                    const tableBody = document.getElementById('server-list');
                    tableBody.innerHTML = '';
                    data.forEach(server => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${server.domain}</td>
                            <td>${server.ip}:${server.port}</td>
                            <td><button onclick="removeServer('${server.domain}')">Remove</button></td>
                        `;
                        tableBody.appendChild(row);
                    });
                });
        };

        document.getElementById('add-server-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const formData = new FormData(e.target);

            fetch('/admin/add-server', {
                method: 'POST',
                body: formData
            }).then(response => response.json()).then(data => {
                alert(data.message);
                loadServers();
            });
        });

        window.removeServer = (domain) => {
            fetch('/admin/remove-server', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ domain })
            }).then(response => response.json()).then(data => {
                alert(data.message);
                loadServers();
            });
        };

        loadServers();
    }

    // Handle add server button click to open the pop-up
    if (document.getElementById('add-server-button')) {
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
});
