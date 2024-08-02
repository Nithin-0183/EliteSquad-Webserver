<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat Application</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f0f0;
        }
        header {
            background-color: #2c3e50;
            color: #fff;
            padding: 10px 0;
            text-align: center;
        }
        .container {
            margin-top: 20px;
        }
        .chat-container {
            background-color: #fff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .chat-messages {
            height: 300px;
            overflow-y: scroll;
            margin-bottom: 20px;
        }
        .chat-message {
            margin-bottom: 10px;
        }
        footer {
            background-color: #2c3e50;
            color: #fff;
            text-align: center;
            padding: 10px 0;
            position: fixed;
            width: 100%;
            bottom: 0;
        }
    </style>
</head>
<body>

<header>
    <h1>Chat Application</h1>
</header>

<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-8 chat-container">
            <div class="chat-messages" id="chat-messages">
                <!-- 메시지 표시 -->
            </div>
            <form id="chat-form">
                <div class="form-group">
                    <label for="name">Name</label>
                    <input type="text" class="form-control" id="name" name="name" placeholder="Enter your name" required>
                </div>
                <div class="form-group">
                    <label for="message">Message</label>
                    <input type="text" class="form-control" id="message" name="message" placeholder="Enter your message" required>
                </div>
                <button type="submit" class="btn btn-primary">Send</button>
            </form>
        </div>
    </div>
</div>

<footer>
    &copy; <?php echo date("Y"); ?> Chat Application. All rights reserved.
</footer>

<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
<script>
    $(document).ready(function() {
        $('#chat-form').on('submit', function(event) {
            event.preventDefault();

            const name = $('#name').val();
            const message = $('#message').val();

            $.ajax({
                type: 'POST',
                url: '/api/chat',
                data: { name: name, message: message },
                success: function(response) {
                    $('#chat-messages').append('<div class="chat-message"><strong>' + response.time + ' ' + response.name + ':</strong> ' + response.message + '</div>');
                    $('#message').val('');
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    console.log('Error: ' + textStatus + ' ' + errorThrown);
                }
            });
        });
    });
</script>

</body>
</html>