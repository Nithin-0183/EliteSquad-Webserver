<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat Application</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <h1 class="mt-5">Chat Application</h1>
    <div id="chat-box" class="border p-3 mb-3" style="height: 300px; overflow-y: scroll;">
        <!-- messages -->
    </div>
    <form id="message-form">
        <div class="form-group">
            <input type="text" id="username" class="form-control" placeholder="Your Name" required>
        </div>
        <div class="form-group">
            <textarea id="message" class="form-control" rows="3" placeholder="Type your message here..." required></textarea>
        </div>
        <button type="submit" class="btn btn-primary">Send</button>
    </form>
    <button id="refresh-btn" class="btn btn-secondary mt-3">Refresh Chat</button>
</div>

<div class="modal fade" id="editModal" tabindex="-1" role="dialog" aria-labelledby="editModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="editModalLabel">Edit Message</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="editMessageId">
                <div class="form-group">
                    <input type="text" id="editUsername" class="form-control" placeholder="Your Name" required>
                </div>
                <div class="form-group">
                    <textarea id="editMessage" class="form-control" rows="3" placeholder="Edit your message..." required></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" id="saveEditButton">Save changes</button>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
<script>
    const apiUrl = 'https://site2.local:8443'; // API

    // fetch messages
    function fetchMessages() {
        fetch(`${apiUrl}/data/messages`)
            .then(response => response.json())
            .then(data => {
                const chatBox = document.getElementById('chat-box');
                chatBox.innerHTML = '';
                data.forEach(message => {
                    chatBox.innerHTML += `
                        <div>
                            <strong>${message.username}:</strong> ${message.text}
                            <button class="btn btn-sm btn-warning" onclick="editMessage('${message.id}', '${message.username}', '${message.text}')">Edit</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteMessage('${message.id}')">Delete</button>
                        </div>`;
                });
                chatBox.scrollTop = chatBox.scrollHeight; // Scroll to bottom
            });
    }

    // send a new message
    const postMessage = (username, text) => {
        fetch(`${apiUrl}/data/messages`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `username=${encodeURIComponent(username)}&text=${encodeURIComponent(text)}`
        }).then(response => response.json())
        .then(() => fetchMessages());
    };

    // delete a message
    function deleteMessage(id) {
        fetch(`${apiUrl}/data/messages/${id}`, {
            method: 'DELETE'
        }).then(() => fetchMessages());
    }

    // update a message
    function updateMessage(id, username, text) {
        fetch(`${apiUrl}/data/messages/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `username=${encodeURIComponent(username)}&text=${encodeURIComponent(text)}`
        }).then(() => fetchMessages());
    }

    // edit message
    function editMessage(id, username, text) {
        document.getElementById('editMessageId').value = id;
        document.getElementById('editUsername').value = username;
        document.getElementById('editMessage').value = text;
        $('#editModal').modal('show');
    }

    document.getElementById('message-form').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const message = document.getElementById('message').value;
        postMessage(username, message);
        document.getElementById('message-form').reset();
    });

    document.getElementById('refresh-btn').addEventListener('click', fetchMessages);

    document.getElementById('saveEditButton').addEventListener('click', function() {
        const id = document.getElementById('editMessageId').value;
        const username = document.getElementById('editUsername').value;
        const message = document.getElementById('editMessage').value;
        updateMessage(id, username, message);
        $('#editModal').modal('hide');
    });

    // Polling to fetch messages every 5 seconds
    setInterval(fetchMessages, 5000);

    // fetch messages on initial load
    fetchMessages();
    
</script>
</body>
</html>
