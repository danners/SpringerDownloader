SpringerDownloader
==================

This tool written in Java is able to download books from [SpringerLink](http://www.springerlink.com) and merge the individual pdf files per chapter in one pdf while creating bookmarks with the correct chapter names. It relies on jSoup for parsing the website und Apache PDFBox for PDF-merging and bookmark creation.

Only books, which you have full access to can be downloaded. This is often the case if you are student and your university has bought a license. If your university offers VPN access, just connect to the VPN and you should be able to download books. Book urls should be of the form:
http://www.springerlink.com/content/978-3-642-21305-2/contents/

You must have full access to all chapters.
