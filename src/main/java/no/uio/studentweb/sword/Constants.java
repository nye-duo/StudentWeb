/*
	Copyright (c) 2012, University of Oslo

	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:
		* Redistributions of source code must retain the above copyright
		  notice, this list of conditions and the following disclaimer.
		* Redistributions in binary form must reproduce the above copyright
		  notice, this list of conditions and the following disclaimer in the
		  documentation and/or other materials provided with the distribution.
		* Neither the name of the University of Oslo nor the
		  names of its contributors may be used to endorse or promote products
		  derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF OSLO BE LIABLE FOR ANY
	DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.uio.studentweb.sword;

public class Constants
{
    /** Namespace for the StudentWeb/FS metadata */
	public static String FS_NS = "http://studentweb.no/terms/";

    /** Name of the root element of the FS metadata schema */
    public static String FS_METADATA = "metadata";

    /** The FS metadata element in which the grade is stored */
	public static String FS_GRADE = "grade";

    /** The FS metadata element in which the embargo end date is stored */
	public static String FS_EMBARGO_END_DATE = "embargoEndDate";

    /** The FS metadata element in which the embargo type is stored */
	public static String FS_EMBARGO_TYPE = "embargoType";

    /** String to use to describe a permanent embargo in the FS metadata */
    public static String EMBARGO_PERMANENT = "restricted";

    /** String to use to describe an un-embargoed item in the FS metadata */
    public static String EMBARGO_OPEN = "open";

    /** String to use to describe an item embargoed for 3 years in the FS metadata */
    public static String EMBARGO_3_YEARS = "3 years";

    /** String to use to describe an item that is indefinitely embargoed in the FS metadata */
    public static String EMBARGO_CLOSED = "closed";

    /** String to describe a pass grade in the FS metadata */
    public static String PASS = "pass";

    /** String to describe a fail grade in the FS metadata */
    public static String FAIL = "fail";
}
