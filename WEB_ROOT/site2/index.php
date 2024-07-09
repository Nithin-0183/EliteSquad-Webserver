<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Blog</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f0f0f0;
        }
        header {
            background-color: #2c3e50;
            color: #fff;
            padding: 10px 0;
            text-align: center;
        }
        nav {
            display: flex;
            justify-content: center;
            background-color: #34495e;
        }
        nav a {
            color: #fff;
            padding: 14px 20px;
            text-decoration: none;
            text-align: center;
        }
        nav a:hover {
            background-color: #2c3e50;
        }
        .container {
            display: flex;
            flex-wrap: wrap;
            max-width: 1200px;
            margin: auto;
            padding: 20px;
            background-color: #fff;
        }
        .main-content {
            flex: 3;
            margin-right: 20px;
        }
        .sidebar {
            flex: 1;
            background-color: #ecf0f1;
            padding: 20px;
            border-radius: 8px;
        }
        .article {
            margin-bottom: 20px;
            padding: 20px;
            background-color: #ecf0f1;
            border-radius: 8px;
        }
        .article h2 {
            margin-top: 0;
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
    <h1>My Blog</h1>
</header>

<nav>
    <a href="#">Home</a>
    <a href="#">News</a>
    <a href="#">About</a>
    <a href="#">Contact</a>
</nav>

<div class="container">
    <div class="main-content">
        <div class="article">
            <h2>Article 1</h2>
            <p>
                <?php
                // PHP code to fetch and display the content of Article 1
                echo "This is the content of the first article. It can be dynamically fetched from a database.";
                ?>
            </p>
        </div>
        <div class="article">
            <h2>Article 2</h2>
            <p>
                <?php
                // PHP code to fetch and display the content of Article 2
                echo "This is the content of the second article. It can be dynamically fetched from a database.";
                ?>
            </p>
        </div>
    </div>

    <aside class="sidebar">
        <h3>Recent Posts</h3>
        <ul>
            <li><a href="#">Post 1</a></li>
            <li><a href="#">Post 2</a></li>
            <li><a href="#">Post 3</a></li>
            <li><a href="#">Post 4</a></li>
        </ul>
    </aside>
</div>

<footer>
    &copy; <?php echo date("Y"); ?> My Blog. All rights reserved.
</footer>

</body>
</html>